package ftn.app.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import ftn.app.dto.*;
import ftn.app.mapper.UserFullDTOMapper;
import ftn.app.model.Provider;
import ftn.app.model.Role;
import ftn.app.model.enums.EventType;
import ftn.app.repository.RoleRepository;
import ftn.app.service.CaptchaService;
import ftn.app.service.UserService;
import ftn.app.util.LoggingUtil;
import ftn.app.util.TokenUtils;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.management.BadAttributeValueExpException;
import javax.validation.Valid;
import java.io.Console;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;

@CrossOrigin("https://localhost:4200")
@RestController
@RequestMapping(value = "/api/user")
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final TokenUtils tokenUtils;
    private final MessageSource messageSource;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final CaptchaService captchaService;

    public UserController(AuthenticationManager authenticationManager,
                          TokenUtils tokenUtils,
                          MessageSource messageSource,
                          UserService userService,
                          RoleRepository roleRepository,
                          CaptchaService captchaService) {
        this.authenticationManager = authenticationManager;
        this.tokenUtils = tokenUtils;
        this.messageSource = messageSource;
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.captchaService = captchaService;
    }

    @PostMapping(value = "/login", consumes = "application/json")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginInfo) {
        LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.REQUEST,"attempted logging in after two factor confirmation");
        try {
            String response = loginInfo.getCaptcha();
            captchaService.processResponse(response);
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
        } catch (BadAttributeValueExpException ex){
            LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.FAIL,"request for login failed. Captcha error");
            return new ResponseEntity<>(messageSource.getMessage("captcha.Error", null, Locale.getDefault()), HttpStatus.NOT_FOUND);
        }
    }

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String password;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String idClient;

    @PostMapping(value = "/loginWithGoogle", consumes = "application/json")
    public ResponseEntity<?> loginWithGoogle(@Valid @RequestBody String credential){
        NetHttpTransport transport = new NetHttpTransport();
        JacksonFactory factory = JacksonFactory.getDefaultInstance();
        GoogleIdTokenVerifier.Builder ver =
                new GoogleIdTokenVerifier.Builder(transport,factory)
                        .setAudience(Collections.singleton(idClient));
        GoogleIdToken googleIdToken = null;
        try {
            googleIdToken = GoogleIdToken.parse(ver.getJsonFactory(),credential);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        GoogleIdToken.Payload payload = googleIdToken.getPayload();
        String email = payload.getEmail();
        User user = new User();
        if(userService.ifEmailExist(email)){
            user = userService.getUserFromEmail(email);
        } else {
            user = new User();
            user.setEmail(email);
            user.setProvider(Provider.GOOGLE);
            user.setPassword(password);
            userService.register(user);
        }
        String jwt = tokenUtils.generateToken(user.getUsername(), (user.getRoles()).get(0));
        LoggingUtil.LogEvent(user.getEmail(), EventType.SUCCESS,"request for login succeeded. User is logged in");
        return new ResponseEntity<>(new TokenDTO(jwt), HttpStatus.OK);
    }

    @PostMapping(value = "/login/sendEmail", consumes = "application/json")
    public ResponseEntity<?> login1(@Valid @RequestBody LoginDTO loginInfo) {
        LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.REQUEST,"attempted logging in via email confirmation");
        try {
            String response = loginInfo.getCaptcha();
            captchaService.processResponse(response);

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
        } catch (BadAttributeValueExpException ex){
            LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.FAIL,"request for login failed. Captcha error");
            return new ResponseEntity<>(messageSource.getMessage("captcha.Error", null, Locale.getDefault()), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/login/sendMessage", consumes = "application/json")
    public ResponseEntity<?> loginWhatsapp(@Valid @RequestBody LoginDTO loginInfo) {
        LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.REQUEST,"attempted logging in via WhatsApp confirmation");
        try {
            String response = loginInfo.getCaptcha();
            captchaService.processResponse(response);
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
        }catch (BadAttributeValueExpException ex){
            LoggingUtil.LogEvent(loginInfo.getEmail(), EventType.FAIL,"request for login failed. Captcha error");
            return new ResponseEntity<>(messageSource.getMessage("captcha.Error", null, Locale.getDefault()), HttpStatus.NOT_FOUND);
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
            user.setProvider(Provider.LOCAL);
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
