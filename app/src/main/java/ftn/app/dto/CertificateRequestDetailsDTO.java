package ftn.app.dto;


import ftn.app.model.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateRequestDetailsDTO extends CertificateRequestDTO{

    private RequestStatus requestStatus;
    private Date dateRequested;
    private String denialReason;
    private UserBasicInfoDTO requester;

}
