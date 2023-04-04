package ftn.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserFullDTO {
    @Valid
    @NotNull
    private String email;
    @Valid
    @NotNull
    private String password;
    @Valid
    @NotNull
    private String name;
    @Valid
    @NotNull
    private String surname;
    @Valid
    @NotNull
    private String phoneNumber;
}
