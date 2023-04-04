package ftn.app.dto;

import ftn.app.model.enums.CertificateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateDetailsDTO {
    private String ownerEmail;
    private String certificateType;
    private Date validFrom;
}
