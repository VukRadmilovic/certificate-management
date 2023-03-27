package ftn.app.service;

import ftn.app.repository.UserRepository;
import ftn.app.model.User;
import ftn.app.service.interfaces.IUserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

    @Override
    public User findByEmail(String email) {
        return (userRepository.findByEmail(email)).get();
    }
}
