package ftn.app.dto;

import ftn.app.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBasicInfoDTO {

    private String fullName;
    private String email;

    public UserBasicInfoDTO(User user) {
        this.fullName = user.getName() + " " + user.getSurname();
        this.email = user.getEmail();
    }
}
