package ftn.app.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateRequestDenialDTO {
    @Length(max = 255, message = "{maxLength}")
    @NotEmpty
    private String denialReason;
}
