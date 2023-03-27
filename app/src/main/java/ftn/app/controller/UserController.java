package ftn.app.controller;

import ftn.app.dto.LoginDTO;
import ftn.app.dto.UserDTO;
import ftn.app.service.interfaces.IUserService;
import org.springframework.http.ResponseEntity;
import ftn.app.model.User;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping(value = "/api/user")
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService){
        this.userService = userService;
    }

    @PostMapping(value = "/login", consumes = "application/json")
    public ResponseEntity<?>login(@RequestBody LoginDTO loginInfo){
        User user = userService.findByEmail(loginInfo.getEmail());
        return ResponseEntity.ok(new UserDTO(user.getEmail(), user.getPassword()));
    }
}
