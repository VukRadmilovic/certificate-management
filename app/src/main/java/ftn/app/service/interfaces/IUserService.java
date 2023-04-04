package ftn.app.service.interfaces;

import ftn.app.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface IUserService extends UserDetailsService {
    public User Register(User user);
}
