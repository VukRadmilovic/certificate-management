package ftn.app.service;

import ftn.app.dto.CertificateDetailsDTO;
import ftn.app.dto.CertificateRequestDTO;
import ftn.app.dto.WithdrawingReasonDTO;
import ftn.app.mapper.CertificateDetailsDTOMapper;
import ftn.app.model.*;
import ftn.app.model.enums.CertificateType;
import ftn.app.model.enums.RequestStatus;
import ftn.app.repository.CertificateRepository;
import ftn.app.repository.CertificateRequestRepository;
import ftn.app.repository.UserRepository;
import ftn.app.service.interfaces.ICertificateService;
import ftn.app.util.DateUtil;
import ftn.app.util.KeystoreUtils;
import ftn.app.util.OrganizationDataUtils;
import ftn.app.util.certificateUtils.CertificateDataUtils;
import ftn.app.util.certificateUtils.CertificateUtils;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.*;

@Service
public class CertificateService implements ICertificateService {

    private final CertificateRepository certificateRepository;
    private final CertificateRequestRepository certificateRequestRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final CertificateDataUtils certificateDataUtils;
    private final CertificateUtils certificateUtils;
    private final KeystoreUtils keystoreUtils;

    public CertificateService(CertificateRepository certificateRepository,
                              CertificateRequestRepository certificateRequestRepository,
                              UserRepository userRepository,
                              MessageSource messageSource,
                              CertificateDataUtils certificateDataUtils,
                              CertificateUtils certificateUtils,
                              KeystoreUtils keystoreUtils){
        this.certificateRepository = certificateRepository;
        this.certificateRequestRepository = certificateRequestRepository;
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

    @Override
    public Certificate withdraw(User user, String certificateSerialNumber, WithdrawingReasonDTO reason) {
        Optional<Certificate> certificateOpt = certificateRepository.findBySerialNumber(certificateSerialNumber);
        if(certificateOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("certificate.doesNotExist", null, Locale.getDefault()));
        }
        Certificate certificate = certificateOpt.get();
        if(!certificate.getOwnerEmail().equals(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("certificate.userNotOwner", null, Locale.getDefault()));
        }
        if(!certificate.isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("certificate.alreadyWithdrawn", null, Locale.getDefault()));
        }
        withdrawCertificateTree(certificate);
        certificate.setWithdrawingReason(reason.getReason());
        certificateRepository.save(certificate);
        return certificate;
    }

    private void withdrawCertificateTree(Certificate root) {

        root.setValid(false);
        certificateRepository.save(root);
        List<CertificateRequest> relatedRequests = certificateRequestRepository.findByIssuerSerialNumberAndRequestStatusEquals(root.getSerialNumber(),RequestStatus.PENDING);
        for(CertificateRequest request : relatedRequests) {
            request.setRequestStatus(RequestStatus.WITHDRAWN);
            certificateRequestRepository.save(request);
        }
        List<Certificate> children = certificateRepository.findByIssuerSerialNumber(root.getSerialNumber());
        if(children.size() == 0)  return;
        for(Certificate child : children) {
            withdrawCertificateTree(child);
        }
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

    @Override
    public ByteArrayResource getCertificate(String serialNumber) {
        Optional<Certificate> certificateOpt = certificateRepository.findBySerialNumber(serialNumber);
        if (certificateOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("certificate.doesNotExist", null, Locale.getDefault()));
        }
        Certificate certificate = certificateOpt.get();

        Optional<User> ownerOpt = userRepository.findByEmail(certificate.getOwnerEmail());
        if (ownerOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("user.doesNotExist", null, Locale.getDefault()));
        }
        String alias = generateAlias(ownerOpt.get(), certificate);

        java.security.cert.Certificate fullCertificate = keystoreUtils.readCertificate(alias);
        try {
            return new ByteArrayResource(fullCertificate.getEncoded());
        } catch (CertificateEncodingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messageSource.getMessage("certificate.encodingError", null, Locale.getDefault()));
        }
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
