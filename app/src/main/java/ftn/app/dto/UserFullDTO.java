package ftn.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserFullDTO {
    private String email;
    private String password;
    private String name;
    private String surname;
    private String phoneNumber;
}
