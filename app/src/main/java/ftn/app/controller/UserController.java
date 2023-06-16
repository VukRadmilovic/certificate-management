package ftn.app.controller;

import ftn.app.dto.*;
import ftn.app.mapper.UserFullDTOMapper;
import ftn.app.model.Role;
import ftn.app.model.enums.EventType;
import ftn.app.repository.RoleRepository;
import ftn.app.service.UserService;
import ftn.app.util.LoggingUtil;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
        LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.REQUEST,"attempted logging in after two factor confirmation");
        try {
            User tempUser = new User();
            tempUser.setEmail(loginInfo.getEmail());
            userService.confirmation(tempUser, loginInfo.getConfirmation());
            userService.isConfirmed(loginInfo);
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginInfo.getEmail(), loginInfo.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();
            long diffInMillies = Math.abs((new Date()).getTime() - user.getLastPasswordResetDate().getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            //BEST PRACTICE - 90 DANA TRAJE JEDNA LOZINKA
            if(diff >= 90) {
                LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.FAIL,"request for login failed. Password expired");
                return new ResponseEntity<>(messageSource.getMessage("password.expired",null, Locale.getDefault()), HttpStatus.BAD_REQUEST);
            }
            String jwt = tokenUtils.generateToken(user.getUsername(), (user.getRoles()).get(0));
            LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.SUCCESS,"request for login succeeded. User is logged in");
            return new ResponseEntity<>(new TokenDTO(jwt), HttpStatus.OK);
        } catch (ExpiredJwtException ex) {
            LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.FAIL,"request for login failed. Token expired");
            return new ResponseEntity<>(messageSource.getMessage("jwt.ExpiredToken", null, Locale.getDefault()), HttpStatus.UNAUTHORIZED);
        } catch (BadCredentialsException ex) {
            LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.FAIL,"request for login failed. The user does not exist");
            return new ResponseEntity<>(messageSource.getMessage("user.notFound", null, Locale.getDefault()), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/login/sendEmail", consumes = "application/json")
    public ResponseEntity<?> login1(@Valid @RequestBody LoginDTO loginInfo) {
        LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.REQUEST,"attempted logging in via email confirmation");
        try {
            userService.isConfirmed(loginInfo);
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginInfo.getEmail(), loginInfo.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();
            tokenUtils.generateToken(user.getUsername(), (user.getRoles()).get(0));
            userService.sendConfirmationEmail(user);
            LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.SUCCESS,"request for login processed. Email confirmation sent to " + loginInfo.getEmail() + " email");
            return new ResponseEntity<>(messageSource.getMessage("user.confirmIdentity", null, Locale.getDefault()), HttpStatus.OK);
        } catch (ExpiredJwtException ex) {
            LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.FAIL,"request for login failed. Token expired");
            return new ResponseEntity<>(messageSource.getMessage("jwt.ExpiredToken", null, Locale.getDefault()), HttpStatus.UNAUTHORIZED);
        } catch (BadCredentialsException ex) {
            LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.FAIL,"request for login failed. The user does not exist");
            return new ResponseEntity<>(messageSource.getMessage("user.notFound", null, Locale.getDefault()), HttpStatus.NOT_FOUND);
        } catch (ResponseStatusException ex) {
            LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.FAIL,"request for login failed. The user does not exist");
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @PostMapping(value = "/login/sendMessage", consumes = "application/json")
    public ResponseEntity<?> loginWhatsapp(@Valid @RequestBody LoginDTO loginInfo) {
        LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.REQUEST,"attempted logging in via WhatsApp confirmation");
        try {
            userService.isConfirmed(loginInfo);
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginInfo.getEmail(), loginInfo.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();
            tokenUtils.generateToken(user.getUsername(), (user.getRoles()).get(0));
            userService.sendConfirmationMessage(user);
            LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.SUCCESS,"request for login processed. Message confirmation sent to user's phone number");
            return new ResponseEntity<>(messageSource.getMessage("user.confirmIdentity", null, Locale.getDefault()), HttpStatus.OK);
        } catch (ExpiredJwtException ex) {
            LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.FAIL,"request for login failed. Token expired");
            return new ResponseEntity<>(messageSource.getMessage("jwt.ExpiredToken", null, Locale.getDefault()), HttpStatus.UNAUTHORIZED);
        } catch (BadCredentialsException ex) {
            LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.FAIL,"request for login failed. The user does not exist");
            return new ResponseEntity<>(messageSource.getMessage("user.notFound", null, Locale.getDefault()), HttpStatus.NOT_FOUND);
        } catch (ResponseStatusException ex) {
            LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.FAIL,"request for login failed. The user does not exist");
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }

    @PostMapping(value = "/register/wEmail", consumes = "application/json")
    public ResponseEntity<?> registerWEmail(@Valid @RequestBody UserFullDTO userRegister) {
        LoggingUtil.LogEvent(userRegister.getEmail(), EventType.REQUEST,"attempted registering via email confirmation");
        try {
            User user = UserFullDTOMapper.fromDTOToUser(userRegister);

            List<Role> roles = new ArrayList<>();
            roles.add(roleRepository.findByName("ROLE_AUTHENTICATED").get());
            user.setRoles(roles);
            user.setIsConfirmed(false);
            userService.register(user);
            userService.sendConfirmationEmail(user);
            LoggingUtil.LogEvent(userRegister.getEmail(), EventType.SUCCESS,"request for registration processed. Email confirmation sent to " + userRegister.getEmail() + " email address");
            return new ResponseEntity<>(messageSource.getMessage("user.register", null, Locale.getDefault()), HttpStatus.OK);
        } catch (ResponseStatusException ex) {
            LoggingUtil.LogEvent(userRegister.getEmail(), EventType.FAIL,"request for registration failed. User with that email already exists");
            return new ResponseEntity<>(messageSource.getMessage("user.email.exists", null, Locale.getDefault()), HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping(value = "/register/wMessage", consumes = "application/json")
    public ResponseEntity<?> registerWMessage(@Valid @RequestBody UserFullDTO userRegister) {
        LoggingUtil.LogEvent(userRegister.getEmail(), EventType.REQUEST,"attempted registering via WhatsApp confirmation");
        try {
            User user = UserFullDTOMapper.fromDTOToUser(userRegister);

            List<Role> roles = new ArrayList<>();
            roles.add(roleRepository.findByName("ROLE_AUTHENTICATED").get());
            user.setRoles(roles);
            user.setIsConfirmed(false);
            userService.register(user);
            userService.sendConfirmationMessage(user);
            LoggingUtil.LogEvent(userRegister.getEmail(), EventType.SUCCESS,"request for registration processed. Message confirmation sent to user's phone number");
            return new ResponseEntity<>(messageSource.getMessage("user.register", null, Locale.getDefault()), HttpStatus.OK);
        } catch (ResponseStatusException ex) {
            LoggingUtil.LogEvent(userRegister.getEmail(), EventType.FAIL,"request for registration failed. User with that email already exists");
            return new ResponseEntity<>(messageSource.getMessage("user.email.exists", null, Locale.getDefault()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/register", consumes = "application/json")
    public ResponseEntity<?> registerStep2(@Valid @RequestBody UserFullWithConfirmationDTO userRegister) {
        LoggingUtil.LogEvent(userRegister.getEmail(), EventType.REQUEST,"attempted registering after two factor confirmation");
        try {
            User user = new User();
            user.setEmail(userRegister.getEmail());
            userService.registerConfirmation(user, userRegister.getConfirmation());
            LoggingUtil.LogEvent(userRegister.getEmail(), EventType.SUCCESS,"request for registration succeeded. User registered");
            return new ResponseEntity<>(messageSource.getMessage("user.register", null, Locale.getDefault()), HttpStatus.OK);
        } catch (BadCredentialsException ex) {
            LoggingUtil.LogEvent(userRegister.getEmail(), EventType.FAIL,"request for registration failed. Invalid confirmation code");
            return new ResponseEntity<>(messageSource.getMessage("user.invalidConfirmation", null, Locale.getDefault()), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/passwordReset/sendEmail", consumes = "application/json")
    public ResponseEntity<?> passwordResetSendEmail(@Valid @RequestBody PasswordConfirmationDTO dto) {
        LoggingUtil.LogEvent(dto.getEmail(), EventType.REQUEST,"attempted password reset via email confirmation");
        try {
            userService.loadUserByUsername(dto.getEmail());
            User user = new User();
            user.setEmail(dto.getEmail());
            userService.sendConfirmationEmail(user);
            LoggingUtil.LogEvent(dto.getEmail(), EventType.SUCCESS,"request for password reset processed. Email confirmation sent to " + dto.getEmail() + " email address");
            return new ResponseEntity<>(messageSource.getMessage("user.passwordReset.emailSent", null, Locale.getDefault()), HttpStatus.OK);
        } catch (UsernameNotFoundException ex) {
            LoggingUtil.LogEvent(dto.getEmail(), EventType.FAIL,"request for password reset failed. The user does not exist");
            return new ResponseEntity<>(messageSource.getMessage("user.notFound", null, Locale.getDefault()), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/passwordReset/sendMessage", consumes = "application/json")
    public ResponseEntity<?> passwordResetSendMessage(@Valid @RequestBody PasswordConfirmationDTO dto) {
        LoggingUtil.LogEvent(dto.getEmail(), EventType.REQUEST,"attempted password reset via WhatsApp confirmation");
        try {
            userService.loadUserByUsername(dto.getEmail());
            User user = new User();
            user.setEmail(dto.getEmail());
            userService.sendConfirmationMessage(user);
            LoggingUtil.LogEvent(dto.getEmail(), EventType.SUCCESS,"request for password reset processed. Message confirmation sent to user's phone number");
            return new ResponseEntity<>(messageSource.getMessage("user.passwordReset.emailSent", null, Locale.getDefault()), HttpStatus.OK);
        } catch (UsernameNotFoundException ex) {
            LoggingUtil.LogEvent(dto.getEmail(), EventType.FAIL,"request for password reset failed. The user does not exist");
            return new ResponseEntity<>(messageSource.getMessage("user.notFound", null, Locale.getDefault()), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/passwordReset", consumes = "application/json")
    public ResponseEntity<?> passwordReset(@Valid @RequestBody PasswordConfirmationDTO dto) {
        try {
            User user = new User();
            user.setEmail(dto.getEmail());
            user.setPassword(dto.getPassword());
            user.setLastPasswordResetDate(new Date());
            userService.passwordConfirmation(user, dto.getConfirmation());
            LoggingUtil.LogEvent(dto.getEmail(), EventType.SUCCESS,"request for password reset succeeded. Password changed");
            return new ResponseEntity<>(messageSource.getMessage("user.passwordReset", null, Locale.getDefault()), HttpStatus.OK);
        } catch (BadCredentialsException ex) {
            LoggingUtil.LogEvent(dto.getEmail(), EventType.FAIL,"request for password reset failed. Invalid confirmation code");
            return new ResponseEntity<>(messageSource.getMessage("user.invalidConfirmation", null, Locale.getDefault()), HttpStatus.NOT_FOUND);
        }
        catch(ResponseStatusException ex) {
            LoggingUtil.LogEvent(dto.getEmail(), EventType.FAIL,"request for password reset failed. Invalid password");
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
    }
}
