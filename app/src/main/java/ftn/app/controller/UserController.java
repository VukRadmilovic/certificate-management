package ftn.app.controller;

import ftn.app.dto.LoginDTO;
import ftn.app.dto.TokenDTO;
import ftn.app.dto.UserFullDTO;
import ftn.app.mapper.UserFullDTOMapper;
import ftn.app.model.Role;
import ftn.app.service.UserService;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping(value = "/api/user")
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final TokenUtils tokenUtils;
    private final MessageSource messageSource;
    private final UserService userService;

    public UserController(AuthenticationManager authenticationManager,
                          TokenUtils tokenUtils,
                          MessageSource messageSource,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenUtils = tokenUtils;
        this.messageSource = messageSource;
        this.userService = userService;
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

    @PostMapping(value = "/register", consumes = "application/json")
    public ResponseEntity<?> register(@Valid @RequestBody UserFullDTO userRegister){
        try {
            User user = UserFullDTOMapper.fromDTOToUser(userRegister);
            Collection<Role> roles = new ArrayList<>();
            roles.add(new Role(1L, "ROLE_AUTHENTICATED"));
            userService.Register(user);
            return new ResponseEntity<>(messageSource.getMessage("user.register", null, Locale.getDefault()), HttpStatus.OK);
        }
        catch (ResponseStatusException ex){
            return new ResponseEntity<>(messageSource.getMessage("user.email.exists", null, Locale.getDefault()), HttpStatus.BAD_REQUEST);
        }
    }
}
