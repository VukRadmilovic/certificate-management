package ftn.app.controller;

import ftn.app.dto.*;
import ftn.app.model.Certificate;
import ftn.app.model.Role;
import ftn.app.model.User;
import ftn.app.model.enums.CertificateType;
import ftn.app.model.enums.EventType;
import ftn.app.service.CertificateRequestService;
import ftn.app.service.CertificateService;
import ftn.app.util.LoggingUtil;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping(value = "/api/certificate")
public class CertificateController {


    private final CertificateService certificateService;
    private final MessageSource messageSource;
    private final CertificateRequestService certificateRequestService;

    public CertificateController(CertificateService certificateService,
                                 MessageSource messageSource,
                                 CertificateRequestService certificateRequestService) {
        this.certificateService = certificateService;
        this.messageSource = messageSource;
        this.certificateRequestService = certificateRequestService;
    }

    @PostMapping(value = "/request", consumes = "application/json")
    public ResponseEntity<?> requestCertificate(@Valid @RequestBody CertificateRequestDTO requestDTO) {
        try {
            User requester = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            LoggingUtil.LogEvent(requester.getEmail(), EventType.REQUEST,"attempted requesting certificate creation");
            if(!requester.getRoles().get(0).getName().equals("ROLE_ADMIN") && requestDTO.getCertificateType().equals(CertificateType.ROOT)){
                LoggingUtil.LogEvent(requester.getEmail(), EventType.FAIL,"request for certificate creation failed. User unauthorized");
                return new ResponseEntity<>(messageSource.getMessage("user.notAuthorized", null, Locale.getDefault()), HttpStatus.UNAUTHORIZED);
            }
            CertificateRequestDetailsDTO requestDetailsDTO = certificateRequestService.createRequest(requestDTO,requester);
            LoggingUtil.LogEvent(requester.getEmail(), EventType.SUCCESS,"request for certificate creation succeeded. Certificate request created");
            return new ResponseEntity<>(requestDetailsDTO, HttpStatus.OK);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @PutMapping(value = "/request/deny/{id}", consumes = "application/json")
    public ResponseEntity<?> denyCertificateRequest(@PathVariable Integer id,
                                                    @Valid @RequestBody CertificateRequestDenialDTO denialDTO) {
        try {
            User denier = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            LoggingUtil.LogEvent(denier.getEmail(),EventType.REQUEST,"attempted denying certificate creation request");
            CertificateRequestDetailsDTO request = certificateRequestService.denyRequest(id, denialDTO.getDenialReason(), denier);
            LoggingUtil.LogEvent(denier.getEmail(),EventType.SUCCESS,"request for denying certificate creation succeeded. Request for certificate creation denied");
            return new ResponseEntity<>(request, HttpStatus.OK);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @PutMapping(value = "/request/accept/{id}")
    public ResponseEntity<?> acceptCertificateRequest(@PathVariable Integer id) {
        try {
            User accepter = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            LoggingUtil.LogEvent(accepter.getEmail(),EventType.REQUEST,"attempted accepting certificate creation request");
            CertificateRequestDetailsDTO request = certificateRequestService.acceptRequest(id, accepter);
            LoggingUtil.LogEvent(accepter.getEmail(),EventType.SUCCESS,"request for accepting certificate creation succeeded. Request for certificate creation accepted");
            return new ResponseEntity<>(request, HttpStatus.OK);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }
    @GetMapping(value = "/all")
    public ResponseEntity<?> getCertificates() {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LoggingUtil.LogEvent(user.getEmail(),EventType.SUCCESS,"request for retrieving all certificates succeeded. Certificates retrieved");
        return new ResponseEntity<>(certificateService.getAllCertificates(), HttpStatus.OK);
    }

    @GetMapping(value = "/detailedAll")
    public ResponseEntity<?> getDetailedCertificates() {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LoggingUtil.LogEvent(user.getEmail(),EventType.SUCCESS,"request for retrieving all certificates succeeded. Certificates retrieved");
        try{
            List<CertificateDetailsWithUserInfoDTO> list = certificateService.getAllDetailedCertificates();
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch(ResponseStatusException ex) {
            LoggingUtil.LogEvent(user.getEmail(),EventType.FAIL, "request for retrieving all certificates failed. One of the certificates owners does not exist");
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> downloadCertificate(@PathVariable String id) {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            LoggingUtil.LogEvent(user.getEmail(),EventType.REQUEST,"attempted downloading a certificate");
            ByteArrayResource resource = certificateService.getCertificate(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=certificate.crt")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .body(resource);
        } catch (ResponseStatusException ex) {
            LoggingUtil.LogEvent(user.getEmail(),EventType.FAIL,"request for downloading certificate failed. " + ex.getReason().substring(0, ex.getReason().length() - 2));
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @GetMapping(value = "/eligible")
    public ResponseEntity<?> getCertificatesEligibleForIssuing() {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LoggingUtil.LogEvent(user.getEmail(),EventType.SUCCESS,"request for retrieving all eligible certificates for issuing succeeded. Eligible certificates retrieved");
        return new ResponseEntity<>(certificateService.getEligibleCertificatesForIssuing(), HttpStatus.OK);
    }

    @GetMapping(value = "/requests/received")
    public ResponseEntity<?> getReceivedRequests() {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LoggingUtil.LogEvent(user.getEmail(),EventType.SUCCESS,"request for retrieving all certificate requests received succeeded. Received certificate creation requests retrieved");
        return new ResponseEntity<>(certificateRequestService.getReceivedRequests(user), HttpStatus.OK);
    }

    @GetMapping(value = "/requests")
    public ResponseEntity<?> getRequests() {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LoggingUtil.LogEvent(user.getEmail(),EventType.REQUEST,"attempted retrieving all sent certificate creation requests");
        Role role = user.getRoles().get(0);
        if (role.getName().equals("ROLE_AUTHENTICATED")) {
            LoggingUtil.LogEvent(user.getEmail(),EventType.SUCCESS,"request for retrieving sent certificate creation requests succeeded. Sent certificate creation requests retrieved");
            return new ResponseEntity<>(certificateRequestService.getUserRequests(user), HttpStatus.OK);
        } else if (role.getName().equals("ROLE_ADMIN")) {
            LoggingUtil.LogEvent(user.getEmail(),EventType.SUCCESS,"request for retrieving all certificate creation requests succeeded. All certificate creation requests retrieved");
            return new ResponseEntity<>(certificateRequestService.getAllRequests(), HttpStatus.OK);
        } else {
            LoggingUtil.LogEvent(user.getEmail(),EventType.FAIL,"request for retrieving sent certificate creation requests failed. Invalid user role");
            return null;
        }
    }

    @GetMapping(value = "/{id}/validate")
    public ResponseEntity<?> validateCertificate(@PathVariable String id) {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LoggingUtil.LogEvent(user.getEmail(),EventType.REQUEST,"attempted validating a certificate");
        try {
            Boolean valid = certificateService.isValidCertificate(id);
            LoggingUtil.LogEvent(user.getEmail(),EventType.SUCCESS,"request for validating a certificate succeeded. Certificate validated");
            return new ResponseEntity<>(valid, HttpStatus.OK);
        } catch (ResponseStatusException ex) {
            LoggingUtil.LogEvent(user.getEmail(),EventType.FAIL,"request for validating a certificate failed. Certificate does not exist");
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @PostMapping(value = "/validate")
    public ResponseEntity<?> validateCertificate(@RequestParam("file") MultipartFile file) {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LoggingUtil.LogEvent(user.getEmail(),EventType.REQUEST,"attempted validating a certificate via file upload. File size: " + ((double)file.getSize() / 1000000) + " MB");
        try {
            Boolean valid = certificateService.isValidCertificate(file);
            LoggingUtil.LogEvent(user.getEmail(),EventType.SUCCESS,"request for validating a certificate via file upload succeeded. Certificate validated");
            return new ResponseEntity<>(valid, HttpStatus.OK);
        } catch (ResponseStatusException ex) {
            LoggingUtil.LogEvent(user.getEmail(),EventType.FAIL,"request for validating a certificate via file upload failed. " + ex.getReason().substring(0,ex.getReason().length() - 2));
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    /**
     * 
     * @param id - serijski broj sertifikata
     * @return sertifikat koji je povucen
     */
    @PutMapping(value = "/{id}/withdraw")
    public ResponseEntity<?> withdrawCertificate(@PathVariable String id, @Valid @RequestBody WithdrawingReasonDTO reason) {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LoggingUtil.LogEvent(user.getEmail(),EventType.REQUEST,"attempted withdrawing a certificate");
        try{
            Certificate withdrawn = certificateService.withdraw(user,id,reason);
            LoggingUtil.LogEvent(user.getEmail(),EventType.SUCCESS,"request for withdrawing a request succeeded. Certificate withdrawn");
            return new ResponseEntity<>(withdrawn, HttpStatus.OK);
        }
        catch(ResponseStatusException ex) {
            LoggingUtil.LogEvent(user.getEmail(),EventType.FAIL,"request for withdrawing a certificate failed. " + ex.getReason().substring(0,ex.getReason().length() - 2));
            return new ResponseEntity<>(ex.getReason(),ex.getStatus());
        }
    }
}
