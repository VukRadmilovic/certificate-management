package ftn.app.controller;

import ftn.app.dto.CertificateRequestDTO;
import ftn.app.dto.CertificateRequestDetailsDTO;
import ftn.app.model.Certificate;
import ftn.app.model.ResponseMessage;
import ftn.app.model.Role;
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
    @GetMapping(value = "/certificates")
    public ResponseEntity<?> getCertificates(){
        return new ResponseEntity<>(certificateService.getAllCertificates(), HttpStatus.OK);
    }
    @GetMapping(value = "/requests")
    public ResponseEntity<?> getRequests(){
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Role role = user.getRoles().get(0);
        if(role.getName().equals("ROLE_AUTHENTICATED")){
            return new ResponseEntity<>(certificateService.getUserRequests(user), HttpStatus.OK);
        } else if (role.getName().equals("ROLE_ADMIN")) {
            return new ResponseEntity<>(certificateService.getAllRequests(), HttpStatus.OK);
        }else {
            return null;
        }
    }
}
