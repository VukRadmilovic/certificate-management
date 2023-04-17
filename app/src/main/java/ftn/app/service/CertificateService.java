package ftn.app.service;

import ftn.app.dto.CertificateDetailsDTO;
import ftn.app.dto.CertificateRequestDTO;
import ftn.app.mapper.CertificateDetailsDTOMapper;
import ftn.app.model.Certificate;
import ftn.app.model.IssuerData;
import ftn.app.model.SubjectData;
import ftn.app.model.User;
import ftn.app.model.enums.CertificateType;
import ftn.app.repository.CertificateRepository;
import ftn.app.repository.UserRepository;
import ftn.app.service.interfaces.ICertificateService;
import ftn.app.util.DateUtil;
import ftn.app.util.KeystoreUtils;
import ftn.app.util.OrganizationDataUtils;
import ftn.app.util.certificateUtils.CertificateDataUtils;
import ftn.app.util.certificateUtils.CertificateUtils;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.cert.X509Certificate;
import java.util.*;

@Service
public class CertificateService implements ICertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final CertificateDataUtils certificateDataUtils;
    private final CertificateUtils certificateUtils;
    private final KeystoreUtils keystoreUtils;

    public CertificateService(CertificateRepository certificateRepository,
                              UserRepository userRepository,
                              MessageSource messageSource,
                              CertificateDataUtils certificateDataUtils,
                              CertificateUtils certificateUtils,
                              KeystoreUtils keystoreUtils){
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
        this.certificateDataUtils = certificateDataUtils;
        this.certificateUtils = certificateUtils;
        this.keystoreUtils = keystoreUtils;
    }

    /**
     * Rekurzivno prolazi kroz lanac sertifikata i proverava da li su validni
     * @param certificate pocetni sertifikat u lancu
     * @param isOverallValid promenljiva koja cuva stanja validnosti od ranijih irteracija rekurzije
     * @return true ako je ceo lanac validan
     */
    @Override
    public boolean isValidCertificate(Certificate certificate, boolean isOverallValid) {
        if(!isOverallValid) {
            return false;
        }
        boolean hasExpired = certificate.getValidUntil().before(DateUtil.getDateWithoutTime(new Date()));
        boolean isValid = certificate.isValid();
        if(!hasExpired && isValid) {
            if(certificate.getIssuerSerialNumber() != null) {
                Optional<Certificate> issuerOpt = certificateRepository.findBySerialNumber(certificate.getIssuerSerialNumber());
                if(issuerOpt.isEmpty()) {
                    return false;
                }
                Certificate issuer = issuerOpt.get();
                if(certificate.getValidUntil().after(issuer.getValidUntil())) {
                    return false;
                }
                else return isValidCertificate(issuer, true);
            }
        } else {
            isOverallValid = false;
        }
        return isOverallValid;
    }

    @Override
    public boolean isValidCertificate(String serialNumber) {
        // Check if certificate exists
        Optional<Certificate> certificateOpt = certificateRepository.findBySerialNumber(serialNumber);
        if (certificateOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("certificate.doesNotExist", null, Locale.getDefault()));
        }
        Certificate certificate = certificateOpt.get();

        return isValidCertificate(certificate);
    }

    private boolean isValidCertificate(Certificate certificate) {
        return isValidCertificate(certificate, true);
    }

    @Override
    public List<CertificateDetailsDTO> getAllCertificates() {
        List<Certificate> temp = certificateRepository.findAll();
        List<CertificateDetailsDTO> certificateDetailsDTOS = new ArrayList<>();
        for (Certificate c: temp) {
            certificateDetailsDTOS.add(CertificateDetailsDTOMapper.fromCertificateToDTO(c));
        }
        return certificateDetailsDTOS;
    }

    @Override
    public List<CertificateDetailsDTO> getEligibleCertificatesForIssuing() {
        List<Certificate> temp = certificateRepository.findAll();
        List<CertificateDetailsDTO> certificateDetailsDTOS = new ArrayList<>();
        for (Certificate c: temp) {
            if(!this.isValidCertificate(c,true) || c.getCertificateType() == CertificateType.END) continue;
            certificateDetailsDTOS.add(CertificateDetailsDTOMapper.fromCertificateToDTO(c));
        }
        return certificateDetailsDTOS;
    }

    private String generateAlias(User requester, Certificate certificate) {
        return requester.getName() + "_" + requester.getSurname() + "_" + certificate.getSerialNumber();
    }

    private String generatePassword(User requester, Certificate certificate) {
        return generateAlias(requester,certificate) + "KSSec";
    }

    @Override
    public Certificate saveCertificate(CertificateRequestDTO requestDTO, User requester) {
        Certificate issuer = null;
        if(requestDTO.getIssuerSerialNumber() != null) {

            Optional<Certificate> issuerOpt = certificateRepository.findBySerialNumber(requestDTO.getIssuerSerialNumber());
            if (issuerOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("certificate.doesNotExist", null, Locale.getDefault()));
            }
            issuer = issuerOpt.get();
        }
        SubjectData certificateSubject = certificateDataUtils.generateSubjectData(requester, requestDTO.getOrganizationData(), requestDTO.getValidUntil());
        Certificate newCertificate = new Certificate();
        newCertificate.setSerialNumber(certificateSubject.getSerialNumber());
        newCertificate.setCertificateType(requestDTO.getCertificateType());
        newCertificate.setIssuerSerialNumber(requestDTO.getIssuerSerialNumber());
        newCertificate.setValidFrom(certificateSubject.getStartDate());
        newCertificate.setValidUntil(certificateSubject.getEndDate());
        newCertificate.setValid(true);
        newCertificate.setOrganizationData(OrganizationDataUtils.writeOrganizationData(requestDTO.getOrganizationData()));
        newCertificate.setOwnerEmail(requester.getEmail());
        Certificate certificate = certificateRepository.save(newCertificate);

        User issuerOwner = requester;
        if(issuer != null) {
            Optional<User> issuerOwnerOpt = userRepository.findByEmail(issuer.getOwnerEmail());
            if (issuerOwnerOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("request.issuerOwnerNotFound", null, Locale.getDefault()));
            }
            issuerOwner = issuerOwnerOpt.get();
        }
        IssuerData issuerData = certificateDataUtils.generateIssuerData(issuerOwner, requestDTO.getOrganizationData());
        X509Certificate certificateKS = certificateUtils.generateCertificate(certificateSubject, issuerData);
        keystoreUtils.saveCertificate(generateAlias(requester,newCertificate),
                issuerData.getPrivateKey(),
                generatePassword(requester,newCertificate).toCharArray(),
                certificateKS);
        return certificate;
    }
}
