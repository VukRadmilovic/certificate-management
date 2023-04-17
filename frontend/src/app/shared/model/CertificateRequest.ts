import {CertificateType} from "./enums/CertificateType";
import {OrganizationData} from "./OrganizationData";

export interface CertificateRequest {
   issuerSerialNumber: String | null;
   certificateType : CertificateType;
   organizationData : OrganizationData;
   validUntil : Date;
}
