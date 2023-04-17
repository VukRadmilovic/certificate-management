import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CertificateRequestDetails} from "../../shared/model/CertificateRequestDetails";
import {environment} from "../../shared/environments/environment";
import {CertificateDetails} from "../../shared/model/CertificateDetails";

@Injectable({
  providedIn: 'root'
})
export class UserCertificatesService {
  constructor(private http : HttpClient) { }

  public getEligibleForNewRequest() : Observable<CertificateDetails[]> {
    return this.http.get<CertificateDetails[]>(environment.apiURL + 'certificate/eligible');
  }
}
