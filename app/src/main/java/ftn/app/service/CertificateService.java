package ftn.app.service;

import ftn.app.dto.CertificateDetailsDTO;
import ftn.app.dto.CertificateRequestDTO;
import ftn.app.dto.CertificateRequestDetailsDTO;
import ftn.app.mapper.CertificateDetailsDTOMapper;
import ftn.app.mapper.CertificateRequestDTOMapper;
import ftn.app.mapper.CertificateRequestDetailsDTOMapper;
import ftn.app.model.*;
import ftn.app.model.enums.CertificateType;
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
import java.util.*;

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

        if(requestDTO.getValidUntil().before(DateUtil.getDateWithoutTime(new Date()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,messageSource.getMessage("request.dateInvalid", null, Locale.getDefault()));
        }
        Optional<Certificate> issuerOpt;
        Certificate issuer;
        if(requestDTO.getIssuerSerialNumber() != null) {
            issuerOpt = certificateRepository.findBySerialNumber(requestDTO.getIssuerSerialNumber());
            if(issuerOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,messageSource.getMessage("issuer.doesNotExist", null, Locale.getDefault()));
            }
            issuer = issuerOpt.get();
            if(requestDTO.getValidUntil().after(issuer.getValidUntil())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,messageSource.getMessage("request.dateInvalid", null, Locale.getDefault()));
            }
            if(issuer.getCertificateType().equals(CertificateType.END)){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,messageSource.getMessage("certificate.endTypeError", null, Locale.getDefault()));
            }

            if(isValidCertificate(issuer,true)) {
                CertificateRequest request = saveRequest(requestDTO,requester,issuer);
                if(requester.getEmail().equals(issuer.getOwnerEmail()) || requester.getRoles().get(0).getName().equals("ROLE_ADMIN")) {
                    saveCertificate(requestDTO,requester);
                }
                return CertificateRequestDetailsDTOMapper.fromRequestToDTO(request);
            }
            else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,messageSource.getMessage("certificate.notValid", null, Locale.getDefault()));
            }
        }
        else {
            CertificateRequest request = saveRequest(requestDTO,requester,null);
            saveCertificate(requestDTO,requester);
            return CertificateRequestDetailsDTOMapper.fromRequestToDTO(request);
        }
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
    public List<CertificateRequestDetailsDTO> getUserRequests(User user) {
        List<CertificateRequest> temp = certificateRequestRepository.findAll();
        List<CertificateRequestDetailsDTO> certificateRequestDetailsDTOS = new ArrayList<>();
        for (CertificateRequest r:temp) {
            if(r.getRequester().getId() == user.getId())
                certificateRequestDetailsDTOS.add(CertificateRequestDetailsDTOMapper.fromRequestToDTO(r));
        }
        return certificateRequestDetailsDTOS;
    }

    @Override
    public List<CertificateRequestDetailsDTO> getAllRequests() {
        List<CertificateRequest> temp = certificateRequestRepository.findAll();
        List<CertificateRequestDetailsDTO> certificateRequestDetailsDTOS = new ArrayList<>();
        for (CertificateRequest r:temp) {
            certificateRequestDetailsDTOS.add(CertificateRequestDetailsDTOMapper.fromRequestToDTO(r));
        }
        return certificateRequestDetailsDTOS;
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
        if(requester.getRoles().get(0).getName().equals("ROLE_ADMIN")) request.setRequestStatus(RequestStatus.ACCEPTED);
        else if(requester.getEmail().equals(issuer.getOwnerEmail())) request.setRequestStatus(RequestStatus.ACCEPTED);
        else request.setRequestStatus(RequestStatus.PENDING);
        return certificateRequestRepository.save(request);
    }

    @Override
    public Certificate saveCertificate(CertificateRequestDTO requestDTO, User requester) {
        Certificate issuer = null;
        if(requestDTO.getIssuerSerialNumber() != null) {
            issuer = certificateRepository.findBySerialNumber(requestDTO.getIssuerSerialNumber()).get();
        }
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

    @Override
    public CertificateRequestDetailsDTO denyRequest(Integer requestId, String reason, User denier) {
        CertificateRequest request = validateRequestManagementAttempt(requestId, denier);

        request.setRequestStatus(RequestStatus.DENIED);
        request.setDenialReason(reason);
        certificateRequestRepository.save(request);

        return CertificateRequestDetailsDTOMapper.fromRequestToDTO(request);
    }

    @Override
    public CertificateRequestDetailsDTO acceptRequest(Integer requestId, User accepter) {
        CertificateRequest request = validateRequestManagementAttempt(requestId, accepter);

        saveCertificate(CertificateRequestDTOMapper.fromRequestToDTO(request), request.getRequester());
        request.setRequestStatus(RequestStatus.ACCEPTED);
        certificateRequestRepository.save(request);

        return CertificateRequestDetailsDTOMapper.fromRequestToDTO(request);
    }

    private CertificateRequest validateRequestManagementAttempt(Integer requestId, User manager) {
        // Check if request exists
        Optional<CertificateRequest> requestOptional = certificateRequestRepository.findById(requestId);
        if (requestOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("request.doesNotExist", null, Locale.getDefault()));
        }
        CertificateRequest request = requestOptional.get();

        // Check if request is pending
        if (!request.getRequestStatus().equals(RequestStatus.PENDING)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageSource.getMessage("request.notPending", null, Locale.getDefault()));
        }

        // Check if issuer exists
        Optional<Certificate> issuerOptional = certificateRepository.findBySerialNumber(request.getIssuerSerialNumber());
        if (issuerOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, messageSource.getMessage("issuer.doesNotExist", null, Locale.getDefault()));
        }
        Certificate issuer = issuerOptional.get();

        // Check if manager is the issuer certificate's owner
        if (!issuer.getOwnerEmail().equals(manager.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageSource.getMessage("request.userNotIssuer", null, Locale.getDefault()));
        }
        return request;
    }
}
