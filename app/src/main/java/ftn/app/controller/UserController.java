package ftn.app.controller;

import ftn.app.dto.LoginDTO;
import ftn.app.dto.TokenDTO;
import ftn.app.util.TokenUtils;
import io.jsonwebtoken.ExpiredJwtException;
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

    public UserController(AuthenticationManager authenticationManager,
                          TokenUtils tokenUtils,
                          MessageSource messageSource) {
        this.authenticationManager = authenticationManager;
        this.tokenUtils = tokenUtils;
        this.messageSource = messageSource;
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
        }
        catch (ExpiredJwtException ex){
            return new ResponseEntity<>(messageSource.getMessage("jwt.ExpiredToken", null, Locale.getDefault()), HttpStatus.UNAUTHORIZED);
        }
        catch (BadCredentialsException ex) {
            return new ResponseEntity<>(messageSource.getMessage("user.notFound", null, Locale.getDefault()), HttpStatus.NOT_FOUND);
        }
    }
}
