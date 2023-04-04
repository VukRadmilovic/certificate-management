package ftn.app.service;

import ftn.app.dto.CertificateRequestDTO;
import ftn.app.dto.CertificateRequestDetailsDTO;
import ftn.app.mapper.CertificateRequestDTOMapper;
import ftn.app.mapper.CertificateRequestDetailsDTOMapper;
import ftn.app.model.Certificate;
import ftn.app.model.CertificateRequest;
import ftn.app.model.User;
import ftn.app.model.enums.CertificateType;
import ftn.app.model.enums.RequestStatus;
import ftn.app.repository.CertificateRepository;
import ftn.app.repository.CertificateRequestRepository;
import ftn.app.service.interfaces.ICertificateRequestService;
import ftn.app.util.DateUtil;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.Locale;
import java.util.Optional;

@Service
public class CertificateRequestService implements ICertificateRequestService {
    private final CertificateRepository certificateRepository;
    private final MessageSource messageSource;
    private final CertificateRequestRepository certificateRequestRepository;
    private final CertificateService certificateService;

    public CertificateRequestService(CertificateRepository certificateRepository,
                                     MessageSource messageSource,
                                     CertificateRequestRepository certificateRequestRepository,
                                     CertificateService certificateService){
        this.certificateRepository = certificateRepository;
        this.messageSource = messageSource;
        this.certificateRequestRepository = certificateRequestRepository;
        this.certificateService = certificateService;
    }

    @Override
    public CertificateRequestDetailsDTO createRequest(CertificateRequestDTO requestDTO, User requester) {

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

            if(certificateService.isValidCertificate(issuer, true)) {
                CertificateRequest request = saveRequest(requestDTO,requester,issuer);
                if(requester.getEmail().equals(issuer.getOwnerEmail()) || requester.getRoles().get(0).getName().equals("ROLE_ADMIN")) {
                    certificateService.saveCertificate(requestDTO,requester);
                }
                return CertificateRequestDetailsDTOMapper.fromRequestToDTO(request);
            }
            else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,messageSource.getMessage("certificate.notValid", null, Locale.getDefault()));
            }
        } else {
            CertificateRequest request = saveRequest(requestDTO,requester,null);
            certificateService.saveCertificate(requestDTO,requester);
            return CertificateRequestDetailsDTOMapper.fromRequestToDTO(request);
        }
    }

    @Override
    public CertificateRequestDetailsDTO acceptRequest(Integer requestId, User accepter) {
        CertificateRequest request = validateRequestManagementAttempt(requestId, accepter);

        certificateService.saveCertificate(CertificateRequestDTOMapper.fromRequestToDTO(request), request.getRequester());
        request.setRequestStatus(RequestStatus.ACCEPTED);
        certificateRequestRepository.save(request);

        return CertificateRequestDetailsDTOMapper.fromRequestToDTO(request);
    }

    @Override
    public CertificateRequestDetailsDTO denyRequest(Integer requestId, String reason, User denier) {
        CertificateRequest request = validateRequestManagementAttempt(requestId, denier);

        request.setRequestStatus(RequestStatus.DENIED);
        request.setDenialReason(reason);
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

    private CertificateRequest saveRequest(CertificateRequestDTO requestDTO, User requester, Certificate issuer) {
        CertificateRequest request = CertificateRequestDTOMapper.fromDTOToRequest(requestDTO);
        request.setDenialReason(null);
        request.setDateRequested(DateUtil.getDateWithoutTime(new Date()));
        request.setRequester(requester);
        if(requester.getRoles().get(0).getName().equals("ROLE_ADMIN")) request.setRequestStatus(RequestStatus.ACCEPTED);
        else if(requester.getEmail().equals(issuer.getOwnerEmail())) request.setRequestStatus(RequestStatus.ACCEPTED);
        else request.setRequestStatus(RequestStatus.PENDING);
        return certificateRequestRepository.save(request);
    }
}
