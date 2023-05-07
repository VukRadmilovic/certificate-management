package ftn.app.controller;

import ftn.app.dto.LoginDTO;
import ftn.app.dto.TokenDTO;
import ftn.app.dto.UserFullDTO;
import ftn.app.dto.UserFullWithConfirmationDTO;
import ftn.app.mapper.UserFullDTOMapper;
import ftn.app.model.Role;
import ftn.app.repository.RoleRepository;
import ftn.app.service.MailService;
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
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping(value = "/api/user")
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final TokenUtils tokenUtils;
    private final MessageSource messageSource;
    private final UserService userService;
    private final RoleRepository roleRepository;

    public UserController(AuthenticationManager authenticationManager,
                          TokenUtils tokenUtils,
                          MessageSource messageSource,
                          UserService userService,
                          RoleRepository roleRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenUtils = tokenUtils;
        this.messageSource = messageSource;
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @PostMapping(value = "/login", consumes = "application/json")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginInfo) {
        try {
            userService.isConfirmed(loginInfo);
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
    public ResponseEntity<?> registerStep1(@Valid @RequestBody UserFullDTO userRegister){
        try {
            User user = UserFullDTOMapper.fromDTOToUser(userRegister);

            List<Role> roles = new ArrayList<>();
            roles.add(roleRepository.findByName("ROLE_AUTHENTICATED").get());
            user.setRoles(roles);
            user.setIsConfirmed(false);
            userService.register(user);

            userService.sendConfirmationEmail(user);
            return new ResponseEntity<>(messageSource.getMessage("user.register", null, Locale.getDefault()), HttpStatus.OK);
        }
        catch (ResponseStatusException ex){
            return new ResponseEntity<>(messageSource.getMessage("user.email.exists", null, Locale.getDefault()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/confirm", consumes = "application/json")
    public ResponseEntity<?> registerStep2(@Valid @RequestBody UserFullWithConfirmationDTO userRegister){
        try{
            User user = UserFullDTOMapper.fromDTOToUser(userRegister);
            userService.confirmation(user, userRegister.getConfirmation());
            return new ResponseEntity<>(messageSource.getMessage("user.register", null, Locale.getDefault()), HttpStatus.OK);
        }
        catch (BadCredentialsException ex){
            return new ResponseEntity<>(messageSource.getMessage("user.invalidConfirmation", null, Locale.getDefault()), HttpStatus.NOT_FOUND);
        }
    }
}
