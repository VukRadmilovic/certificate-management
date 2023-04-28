import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {CertificateDetailsWithUserInfo} from "./model/CertificateDetailsWithUserInfo";
import {Observable} from "rxjs";
import {environment} from "./environments/environment";

@Injectable({
  providedIn: 'root'
})
export class CertificateService {

  constructor(private http : HttpClient) { }

  public getAll() : Observable<CertificateDetailsWithUserInfo[]> {
    return this.http.get<CertificateDetailsWithUserInfo[]>(environment.apiURL + 'certificate/detailedAll');
  }

  public validate(id: string) : Observable<boolean> {
    return this.http.get<boolean>(environment.apiURL + 'certificate/' + id + '/validate');
  }

  public download(id: string) : Observable<Blob> {
    return this.http.get(environment.apiURL + 'certificate/' + id, {responseType: 'blob'});
  }
}
