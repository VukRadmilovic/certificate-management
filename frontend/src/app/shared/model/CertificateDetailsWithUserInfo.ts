import {OrganizationData} from "./OrganizationData";
import {UserBasicInfo} from "./UserBasicInfo";

export interface CertificateDetailsWithUserInfo {
  serialNumber: string;
  owner: UserBasicInfo;
  certificateType: string;
  validFrom: Date;
  validUntil: Date;
  isValid: boolean;
  withdrawingReason: string;
  organizationData: OrganizationData;
}
