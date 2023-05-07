import {AfterViewInit, Component, ViewChild} from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {MatTableDataSource} from "@angular/material/table";
import {CertificateDetails} from "../../shared/model/CertificateDetails";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {UserCertificatesService} from "../services/user-certificates.service";
import {UserRequestsService} from "../services/user-requests.service";
import {CertificateRequest} from "../../shared/model/CertificateRequest";
import {CertificateType} from "../../shared/model/enums/CertificateType";
import {NotificationsService} from "../../shared/notifications.service";
import {Router} from "@angular/router";
import {timer} from "rxjs";

@Component({
  selector: 'app-new-request-form',
  templateUrl: './new-request-form.component.html',
  styleUrls: ['./new-request-form.component.css']
})
export class NewRequestFormComponent implements AfterViewInit {

  displayedColumns: string[] = ['select','serialNumber', 'certificateType', 'organizationData','validFrom', 'validUntil'];
  dataSource!: MatTableDataSource<CertificateDetails>;
  certificates: CertificateDetails[] = [];
  selectedCertificate!: CertificateDetails;
  minDate = new Date();
  maxDate = new Date();
  isChecked = true;

  constructor(private userCertificateService: UserCertificatesService,
              private userRequestsService: UserRequestsService,
              private notificationService: NotificationsService,
              private router: Router) {}
  @ViewChild(MatPaginator) paginator!: any;
  @ViewChild(MatSort) sort!: any;
  requestForm = new FormGroup({
    issuer: new FormControl( '',[Validators.required]),
    certificateType: new FormControl( '',[Validators.required]),
    orgName: new FormControl('',[Validators.required]),
    orgUnit: new FormControl('',[Validators.required]),
    countryCode: new FormControl('',[Validators.required, Validators.minLength(3),Validators.maxLength(3)]),
    validUntil: new FormControl(new Date(), [Validators.required]),
  });

  types: String[] = [
    "INTERMEDIATE","END"
  ];

  ngAfterViewInit() : void {
    this.userCertificateService.getEligibleForNewRequest().subscribe((res) => {
      this.certificates = res;
      this.dataSource = new MatTableDataSource<CertificateDetails>(this.certificates);
      this.dataSource.paginator = this.paginator;
      this.dataSource.sortingDataAccessor = (item, property) => {
        switch(property) {
          case 'organizationData': return item.organizationData.name + ', ' + item.organizationData.unit + ', ' + item.organizationData.countryCode
          default: // @ts-ignore
            return item[property];
        }
      };
      this.dataSource.sort = this.sort;
    });
  }

  public createRequest() : void {
    if(this.requestForm.valid) {
      const request : CertificateRequest =  {
        issuerSerialNumber : this.selectedCertificate.serialNumber,
        certificateType: CertificateType[<string>this.requestForm.controls['certificateType'].value as CertificateType],
        validUntil: new Date(<Date>this.requestForm.controls['validUntil'].value),
        organizationData : {
          name : <string>this.requestForm.controls['orgName'].value,
          unit : <string>this.requestForm.controls['orgUnit'].value,
          countryCode: <string>this.requestForm.controls['countryCode'].value?.toUpperCase()
        }
      }
      this.userRequestsService.sendRequest(request).subscribe();
      this.notificationService.createNotification("Request successfully sent!");
      timer(1000).subscribe(x => { this.router.navigate(['user-requests']) })
    }
  }

  public changeSelected(row : CertificateDetails) : void {
    this.selectedCertificate = row;
    this.requestForm.controls['issuer'].setValue('not null');
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    this.minDate = tomorrow;
    this.maxDate = row.validUntil;
    this.giveRecommendedDate();
  }

  public handleChecked(event: any) : void {
    if(event.checked) {
      this.isChecked = true;
      this.giveRecommendedDate()
    }
    else {
      this.isChecked = false;
    }
  }
  public giveRecommendedDate() : void {
    if(this.isChecked) {
      const certType = this.requestForm.controls['certificateType'].value;
      let validUntil = new Date();
      if (certType == 'ROOT') {
        validUntil.setFullYear(validUntil.getFullYear() + 10);
      }
      else {
        if (this.selectedCertificate != null) {
          let proposedValidation = new Date(validUntil);
          if (certType == 'INTERMEDIATE') {
            proposedValidation.setFullYear(validUntil.getFullYear() + 3)
          } else {
            proposedValidation.setFullYear(validUntil.getFullYear() + 1)
          }
          if (new Date(this.selectedCertificate.validUntil) < proposedValidation) {
            validUntil = this.selectedCertificate.validUntil;
          } else {
            validUntil = proposedValidation;
          }
        }
      }
      this.requestForm.controls['validUntil'].setValue(validUntil);
    }
    else {
      this.requestForm.controls['validUntil'].setValue(null);
    }
  }
  public checkType(type: String) : void {
    this.giveRecommendedDate();
  }
}
