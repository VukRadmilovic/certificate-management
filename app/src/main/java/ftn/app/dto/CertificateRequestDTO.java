package ftn.app.dto;


import ftn.app.model.OrganizationData;
import ftn.app.model.enums.CertificateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateRequestDTO {

    @NotBlank
    private String issuerSerialNumber;

    @NotBlank
    private CertificateType certificateType;

    @Valid
    private OrganizationData organizationData;

    @Valid
    private Date validUntil;

}
