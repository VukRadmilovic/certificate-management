package ftn.app.service.interfaces;

import org.springframework.security.core.userdetails.UserDetailsService;
import ftn.app.model.User;

public interface IUserService extends UserDetailsService {
    User findByEmail(String email);
}
