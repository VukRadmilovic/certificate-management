package ftn.app.controller;

import ftn.app.dto.CertificateRequestDTO;
import ftn.app.dto.CertificateRequestDenialDTO;
import ftn.app.dto.CertificateRequestDetailsDTO;
import ftn.app.model.Certificate;
import ftn.app.model.CertificateRequest;
import ftn.app.model.ResponseMessage;
import ftn.app.model.User;
import ftn.app.model.enums.CertificateType;
import ftn.app.service.CertificateService;
import ftn.app.util.KeystoreUtils;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Locale;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping(value = "/api/certificate")
public class CertificateController {


    private CertificateService certificateService;
    private MessageSource messageSource;
    private KeystoreUtils keystoreUtils;

    public CertificateController(CertificateService certificateService,
                                 MessageSource messageSource,
                                 KeystoreUtils keystoreUtils) {
        this.certificateService = certificateService;
        this.messageSource = messageSource;
        this.keystoreUtils = keystoreUtils;
    }

    @PostMapping(value = "/request", consumes = "application/json")
    public ResponseEntity<?> requestCertificate(@Valid @RequestBody CertificateRequestDTO requestDTO) {
        try {
            User requester = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(!requester.getRoles().get(0).getName().equals("ROLE_ADMIN") && requestDTO.getCertificateType().equals(CertificateType.ROOT)){
                return new ResponseEntity<>(messageSource.getMessage("user.notAuthorized", null, Locale.getDefault()), HttpStatus.UNAUTHORIZED);
            }
            CertificateRequestDetailsDTO requestDetailsDTO = certificateService.requestCertificate(requestDTO,requester);
            return new ResponseEntity<>(requestDetailsDTO, HttpStatus.OK);
        }
        catch (ResponseStatusException ex) {
            return new ResponseEntity<>(new ResponseMessage(ex.getReason()), ex.getStatus());
        }
    }

    @PutMapping(value = "/request/deny/{id}", consumes = "application/json")
    public ResponseEntity<?> denyCertificateRequest(@PathVariable Integer id,
                                                    @Valid @RequestBody CertificateRequestDenialDTO denialDTO) {
        try {
            User denier = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            CertificateRequestDetailsDTO request = certificateService.denyRequest(id, denialDTO.getDenialReason(), denier);
            return new ResponseEntity<>(request, HttpStatus.OK);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(new ResponseMessage(ex.getReason()), ex.getStatus());
        }
    }

    @PutMapping(value = "/request/accept/{id}")
    public ResponseEntity<?> acceptCertificateRequest(@PathVariable Integer id) {
        try {
            User accepter = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            CertificateRequestDetailsDTO request = certificateService.acceptRequest(id, accepter);
            return new ResponseEntity<>(request, HttpStatus.OK);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(new ResponseMessage(ex.getReason()), ex.getStatus());
        }
    }
}
