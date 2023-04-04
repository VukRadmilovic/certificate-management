package ftn.app.dto;

import ftn.app.model.enums.CertificateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateDetailsDTO {
    @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "{format}")
    @NotBlank
    @Valid
    private String ownerEmail;
    @Length(max = 255, message = "{maxLength}")
    @NotBlank(message = "{required}")
    private String certificateType;
    @NotNull
    @Valid
    private Date validFrom;
}
