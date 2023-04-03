package ftn.app.dto;


import ftn.app.model.OrganizationData;
import ftn.app.model.enums.CertificateType;
import ftn.app.util.OrganizationDataUtils;
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
public class CertificateRequestDTO {

    private String issuerSerialNumber;

    @NotNull
    private CertificateType certificateType;

    @Valid
    @NotNull
    private OrganizationData organizationData;

    @Valid
    @NotNull
    private Date validUntil;

    public void generateOrganizationData(String data) {
        this.organizationData = OrganizationDataUtils.parseOrganizationData(data);
    }

}
