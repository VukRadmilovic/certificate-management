import {CertificateRequest} from "./CertificateRequest";
import {UserBasicInfo} from "./UserBasicInfo";
import {RequestStatus} from "./enums/RequestStatus";

export interface CertificateRequestDetails extends CertificateRequest {
  id : number;
  requestStatus : RequestStatus;
  dateRequested : Date;
  denialReason : String;
  requester : UserBasicInfo;
}
