package ftn.app.controller;

import ftn.app.dto.CertificateRequestDTO;
import ftn.app.dto.CertificateRequestDenialDTO;
import ftn.app.dto.CertificateRequestDetailsDTO;
import ftn.app.model.Role;
import ftn.app.model.User;
import ftn.app.model.enums.CertificateType;
import ftn.app.service.CertificateRequestService;
import ftn.app.service.CertificateService;
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
            if(!requester.getRoles().get(0).getName().equals("ROLE_ADMIN") && requestDTO.getCertificateType().equals(CertificateType.ROOT)){
                return new ResponseEntity<>(messageSource.getMessage("user.notAuthorized", null, Locale.getDefault()), HttpStatus.UNAUTHORIZED);
            }
            CertificateRequestDetailsDTO requestDetailsDTO = certificateRequestService.createRequest(requestDTO,requester);
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
            CertificateRequestDetailsDTO request = certificateRequestService.denyRequest(id, denialDTO.getDenialReason(), denier);
            return new ResponseEntity<>(request, HttpStatus.OK);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @PutMapping(value = "/request/accept/{id}")
    public ResponseEntity<?> acceptCertificateRequest(@PathVariable Integer id) {
        try {
            User accepter = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            CertificateRequestDetailsDTO request = certificateRequestService.acceptRequest(id, accepter);
            return new ResponseEntity<>(request, HttpStatus.OK);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }
    @GetMapping(value = "/all")
    public ResponseEntity<?> getCertificates() {
        return new ResponseEntity<>(certificateService.getAllCertificates(), HttpStatus.OK);
    }

    @GetMapping(value = "/requests/received")
    public ResponseEntity<?> getReceivedRequests() {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return new ResponseEntity<>(certificateRequestService.getReceivedRequests(user), HttpStatus.OK);
    }

    @GetMapping(value = "/requests")
    public ResponseEntity<?> getRequests() {
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Role role = user.getRoles().get(0);
        if (role.getName().equals("ROLE_AUTHENTICATED")) {
            return new ResponseEntity<>(certificateRequestService.getUserRequests(user), HttpStatus.OK);
        } else if (role.getName().equals("ROLE_ADMIN")) {
            return new ResponseEntity<>(certificateRequestService.getAllRequests(), HttpStatus.OK);
        } else {
            return null;
        }
    }

    @GetMapping(value = "/{id}/validate")
    public ResponseEntity<?> validateCertificate(@PathVariable String id) {
        try {
            Boolean valid = certificateService.isValidCertificate(id);
            return new ResponseEntity<>(valid, HttpStatus.OK);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }
}
