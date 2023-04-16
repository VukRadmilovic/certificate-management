import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CertificateRequestDetails} from "../../shared/model/CertificateRequestDetails";
import {environment} from "../../shared/environments/environment";
import {DenialReason} from "../../shared/model/DenialReason";

@Injectable({
  providedIn: 'root'
})
export class UserRequestsService {

  constructor(private http : HttpClient) { }

  public getAll() : Observable<CertificateRequestDetails[]> {
    return this.http.get<CertificateRequestDetails[]>(environment.apiURL + 'certificate/requests');
  }

  public denyRequest(denial : DenialReason, id: string) : Observable<CertificateRequestDetails> {
    return this.http.put<CertificateRequestDetails>(environment.apiURL + "certificate/request/deny/" + id,denial);
  }
}
