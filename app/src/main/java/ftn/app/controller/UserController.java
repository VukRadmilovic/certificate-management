package ftn.app.controller;

import ftn.app.certificateUtils.CertificateDataUtils;
import ftn.app.certificateUtils.CertificateUtils;
import ftn.app.dto.LoginDTO;
import ftn.app.dto.TokenDTO;
import ftn.app.keystoreUtils.KeystoreUtils;
import ftn.app.util.TokenUtils;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ftn.app.model.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Locale;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping(value = "/api/user")
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final TokenUtils tokenUtils;
    private final MessageSource messageSource;
    private final KeystoreUtils keystoreUtils;
    private final CertificateDataUtils certificateDataUtils;
    private final CertificateUtils certificateUtils;

    public UserController(AuthenticationManager authenticationManager,
                          TokenUtils tokenUtils,
                          MessageSource messageSource,
                          KeystoreUtils keystoreUtils,
                          CertificateDataUtils certificateDataUtils,
                          CertificateUtils certificateUtils) {
        this.authenticationManager = authenticationManager;
        this.tokenUtils = tokenUtils;
        this.messageSource = messageSource;
        this.keystoreUtils = keystoreUtils;
        this.certificateDataUtils = certificateDataUtils;
        this.certificateUtils = certificateUtils;
    }

    @PostMapping(value = "/login", consumes = "application/json")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginInfo) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginInfo.getEmail(), loginInfo.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();
            String jwt = tokenUtils.generateToken(user.getUsername(), (user.getRoles()).get(0));
            return new ResponseEntity<>(new TokenDTO(jwt), HttpStatus.OK);
        } catch (BadCredentialsException ex) {
            return new ResponseEntity<>(messageSource.getMessage("user.notFound", null, Locale.getDefault()), HttpStatus.NOT_FOUND);
        }
    }
}
