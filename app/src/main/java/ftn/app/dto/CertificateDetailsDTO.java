package ftn.app.dto;

import ftn.app.model.enums.CertificateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateDetailsDTO {
    @NotBlank
    @Valid
    private String ownerEmail;
    @NotBlank
    @Valid
    private String certificateType;
    @NotNull
    @Valid
    private Date validFrom;
}
