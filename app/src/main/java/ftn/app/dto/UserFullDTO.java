package ftn.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserFullDTO {
    @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "{format}")
    private String email;

    @Length(max = 255, message = "{maxLength}")
    @NotBlank(message = "{required}")
    private String password;

    @Length(max = 255, message = "{maxLength}")
    @NotBlank(message = "{required}")
    private String name;

    @Length(max = 255, message = "{maxLength}")
    @NotBlank(message = "{required}")
    private String surname;

    @Length(max = 255, message = "{maxLength}")
    @NotBlank(message = "{required}")
    private String phoneNumber;
}
