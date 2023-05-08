package ftn.app.service.interfaces;

import ftn.app.dto.LoginDTO;
import ftn.app.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface IUserService extends UserDetailsService {
    User register(User user);
    Boolean confirmation(User user, String confirmation);

    Boolean registerConfirmation(User user, String confirmation);
    Boolean passwordConfirmation(User user, String confirmation);

    void sendConfirmationEmail(User user);

    void sendConfirmationMessage(User user);

    Boolean isConfirmed(LoginDTO loginInfo);

    void sendWhatsappMessage(String number, String message);
}
