package ftn.app.service;

import ftn.app.dto.CertificateRequestDTO;
import ftn.app.dto.CertificateRequestDetailsDTO;
import ftn.app.mapper.CertificateRequestDTOMapper;
import ftn.app.mapper.CertificateRequestDetailsDTOMapper;
import ftn.app.model.*;
import ftn.app.model.enums.RequestStatus;
import ftn.app.repository.CertificateRepository;
import ftn.app.repository.CertificateRequestRepository;
import ftn.app.repository.UserRepository;
import ftn.app.service.interfaces.ICertificateService;
import ftn.app.util.DateUtil;
import ftn.app.util.KeystoreUtils;
import ftn.app.util.certificateUtils.CertificateDataUtils;
import ftn.app.util.certificateUtils.CertificateUtils;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Locale;

@Service
public class CertificateService implements ICertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final CertificateDataUtils certificateDataUtils;
    private final CertificateRequestRepository certificateRequestRepository;
    private final CertificateUtils certificateUtils;
    private final KeystoreUtils keystoreUtils;

    public CertificateService(CertificateRepository certificateRepository,
                              UserRepository userRepository,
                              MessageSource messageSource,
                              CertificateDataUtils certificateDataUtils,
                              CertificateRequestRepository certificateRequestRepository,
                              CertificateUtils certificateUtils,
                              KeystoreUtils keystoreUtils){
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
        this.certificateDataUtils = certificateDataUtils;
        this.certificateRequestRepository = certificateRequestRepository;
        this.certificateUtils = certificateUtils;
        this.keystoreUtils = keystoreUtils;
    }

    @Override
    public CertificateRequestDetailsDTO requestCertificate(CertificateRequestDTO requestDTO, User requester) {

        Certificate issuer = certificateRepository.findBySerialNumber(requestDTO.getIssuerSerialNumber()).get();
        if(requestDTO.getValidUntil().before(DateUtil.getDateWithoutTime(new Date())) || requestDTO.getValidUntil().after(issuer.getValidUntil())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,messageSource.getMessage("request.dateInvalid", null, Locale.getDefault()));
        }
        if(isValidCertificate(issuer,true)) {
            CertificateRequest request = saveRequest(requestDTO,requester,issuer);
            if(requester.getEmail().equals(issuer.getOwnerEmail()) || requester.getRoles().get(0).getName().equals("ROLE_ADMIN")) {
               saveCertificate(requestDTO,requester);
               return CertificateRequestDetailsDTOMapper.fromRequestToDTO(request);
            }
        }
        else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,messageSource.getMessage("certificate.notValid", null, Locale.getDefault()));
        }
        return null;
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
        if(!hasExpired && isValid)
        {
            isOverallValid = true;
            if(certificate.getIssuerSerialNumber() != null) {
                Certificate issuer = certificateRepository.findBySerialNumber(certificate.getIssuerSerialNumber()).get();
                if(certificate.getValidUntil().after(issuer.getValidUntil())) {
                    isOverallValid = false;
                    return false;
                }
                else return isValidCertificate(issuer,isOverallValid);
            }
        }
        else isOverallValid = false;
        return isOverallValid;
    }

    private String generateAlias(User requester, Certificate certificate) {
        return requester.getName() + "_" + requester.getSurname() + "_" + certificate.getSerialNumber();
    }

    private String generatePassword(User requester, Certificate certificate) {
        return generateAlias(requester,certificate) + "KSSec";
    }

    @Override
    public CertificateRequest saveRequest(CertificateRequestDTO requestDTO, User requester, Certificate issuer) {
        CertificateRequest request = CertificateRequestDTOMapper.fromDTOToRequest(requestDTO);
        request.setDenialReason(null);
        request.setDateRequested(DateUtil.getDateWithoutTime(new Date()));
        request.setRequester(requester);
        if(requester.getEmail().equals(issuer.getOwnerEmail())) request.setRequestStatus(RequestStatus.ACCEPTED);
        else if(requester.getRoles().get(0).getName().equals("ROLE_ADMIN")) request.setRequestStatus(RequestStatus.ACCEPTED);
        else request.setRequestStatus(RequestStatus.PENDING);
        return certificateRequestRepository.save(request);
    }

    @Override
    public Certificate saveCertificate(CertificateRequestDTO requestDTO, User requester) {
        Certificate issuer = certificateRepository.findBySerialNumber(requestDTO.getIssuerSerialNumber()).get();
        SubjectData certificateSubject = certificateDataUtils.generateSubjectData(requester, requestDTO.getOrganizationData(), requestDTO.getValidUntil());
        Certificate newCertificate = new Certificate();
        newCertificate.setSerialNumber(certificateSubject.getSerialNumber());
        newCertificate.setCertificateType(requestDTO.getCertificateType());
        newCertificate.setIssuerSerialNumber(requestDTO.getIssuerSerialNumber());
        newCertificate.setValidFrom(certificateSubject.getStartDate());
        newCertificate.setValidUntil(certificateSubject.getEndDate());
        newCertificate.setValid(true);
        newCertificate.setOwnerEmail(requester.getEmail());
        Certificate certificate = certificateRepository.save(newCertificate);

        User issuerOwner = userRepository.findByEmail(issuer.getOwnerEmail()).get();
        IssuerData issuerData = certificateDataUtils.generateIssuerData(issuerOwner, requestDTO.getOrganizationData());
        X509Certificate certificateKS = certificateUtils.generateCertificate(certificateSubject, issuerData);
        keystoreUtils.saveCertificate(generateAlias(requester,newCertificate),
                issuerData.getPrivateKey(),
                generatePassword(requester,newCertificate).toCharArray(),
                certificateKS);
        return certificate;
    }
}
