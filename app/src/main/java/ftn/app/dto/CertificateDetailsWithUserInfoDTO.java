package ftn.app.dto;

import ftn.app.model.OrganizationData;
import ftn.app.model.User;
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
public class CertificateDetailsWithUserInfoDTO {

    private String serialNumber;

    @NotBlank
    @Valid
    private UserBasicInfoDTO owner;
    @Length(max = 255, message = "{maxLength}")
    @NotBlank(message = "{required}")
    private String certificateType;
    @NotNull
    @Valid
    private Date validFrom;
    @NotNull
    @Valid
    private Date validUntil;

    @NotNull
    private boolean isValid;

    @NotBlank
    private String withdrawingReason;
    @NotNull
    @Valid
    private OrganizationData organizationData;

    public CertificateDetailsWithUserInfoDTO(CertificateDetailsDTO certificate, User user) {
        this.serialNumber = certificate.getSerialNumber();
        this.owner = new UserBasicInfoDTO(user);
        this.certificateType = certificate.getCertificateType();
        this.validFrom = certificate.getValidFrom();
        this.validUntil = certificate.getValidUntil();
        this.isValid = certificate.isValid();
        this.withdrawingReason = certificate.getWithdrawingReason();
        this.organizationData = certificate.getOrganizationData();
    }
}
