package ftn.app.service;

import ftn.app.dto.LoginDTO;
import ftn.app.model.Confirmation;
import ftn.app.model.User;
import ftn.app.repository.ConfirmationRepository;
import ftn.app.repository.UserRepository;
import ftn.app.service.interfaces.IUserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final MailService mailService;
    private final ConfirmationRepository confirmationRepository;

    public UserService(UserRepository userRepository,
                       MailService mailService,
                       ConfirmationRepository confirmationRepository){
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.confirmationRepository = confirmationRepository;
    }

    private BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        } else {
            return user.get();
        }
    }

    @Override
    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new ResponseStatusException(HttpStatus.FOUND);
        }
        user.setPassword(passwordEncoder().encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public Boolean confirmation(User user, String confirmation) {
        user = userRepository.findByEmail(user.getEmail()).get();
        Optional<Confirmation> existing = confirmationRepository.findByUserAndExpired(user, false);
        if(existing.isPresent()){
            if(existing.get().getConfirmation().equals(confirmation)){
                confirmationRepository.delete(existing.get());
                user.setIsConfirmed(true);
                userRepository.save(user);
                return true;
            }
            else {
                throw new BadCredentialsException("user.invalidConfirmation");
            }
        }
        else {
            throw new BadCredentialsException("user.invalidConfirmation");
        }
    }

    @Override
    public void sendConfirmationEmail(User user) {
        //Optional<Confirmation> existing = confirmationRepository.findByUserAndExpired(user, false);
        //existing.ifPresent(confirmationRepository::delete);
        int confirmationString = (int) Math.floor(Math.random() * (99999 - 10000 + 1) + 10000);
        Confirmation confirmation = new Confirmation();
        confirmation.setConfirmation(Integer.toString(confirmationString));
        confirmation.setUser(user);
        confirmation.setExpired(false);
        confirmationRepository.save(confirmation);
        mailService.sendSimpleMessage(user.getEmail(), "Email confirmation", confirmation.getConfirmation());
    }

    @Override
    public Boolean isConfirmed(LoginDTO loginInfo) {
        if(userRepository.findByEmail(loginInfo.getEmail()).get().getIsConfirmed()){
            return true;
        }
        throw new BadCredentialsException("Bad credentials");
    }
}