package ftn.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawingReasonDTO {

    @NotBlank(message = "{required}")
    @NotNull
    @Length(max = 300, message = "{maxLength}")
    private String reason;
}
