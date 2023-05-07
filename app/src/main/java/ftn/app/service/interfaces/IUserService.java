package ftn.app.service.interfaces;

import ftn.app.dto.LoginDTO;
import ftn.app.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface IUserService extends UserDetailsService {
    User register(User user);
    Boolean confirmation(User user, String confiramtion);

    void sendConfirmationEmail(User user);

    Boolean isConfirmed(LoginDTO loginInfo);
}
