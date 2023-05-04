import {AfterViewInit, Component, ViewChild} from '@angular/core';
import {MatTableDataSource} from "@angular/material/table";
import {DenialReason} from "../model/DenialReason";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {NotificationsService} from "../notifications.service";
import {CertificateService} from "../certificate.service";
import {CertificateDetailsWithUserInfo} from "../model/CertificateDetailsWithUserInfo";
import {NgxFileDropEntry} from "ngx-file-drop";
import {UserService} from "../user.service";
import {
  RequestDenyReasonDialogComponent
} from "../../user/request-deny-reason-dialog/request-deny-reason-dialog.component";
import {RequestStatus} from "../model/enums/RequestStatus";
import {MatDialog} from "@angular/material/dialog";
import {WithdrawReasonDialogComponent} from "../../user/withdraw-reason-dialog/withdraw-reason-dialog.component";
import {UserCertificatesService} from "../../user/services/user-certificates.service";
import {WithdrawalReason} from "../model/WithdrawalReason";
import {HttpErrorResponse} from "@angular/common/http";
import {CertificateType} from "../model/enums/CertificateType";

@Component({
  selector: 'app-certificates',
  templateUrl: './certificates.component.html',
  styleUrls: ['./certificates.component.css']
})
export class CertificatesComponent implements AfterViewInit {
  displayedColumns: string[] = ['serialNumber', 'certificateType', 'owner.fullName', 'organizationData', 'validFrom',
    'validUntil','withdrawingReason'];
  enableStatusChange: boolean = false;
  enableWithdrawChange: boolean = false;
  dataSource!: MatTableDataSource<CertificateDetailsWithUserInfo>;
  certificates: CertificateDetailsWithUserInfo[] = [];
  selectedCertificate!: CertificateDetailsWithUserInfo;
  withdrawalReason: WithdrawalReason = {
    reason: ''
  };

  @ViewChild(MatPaginator) paginator!: any;
  @ViewChild(MatSort) sort!: any;
  cindex: number = -1;

  constructor(private notificationService: NotificationsService,
              private userService: UserService,
              private certificateService: CertificateService,
              private userCertificateService: UserCertificatesService,
              public withdrawalReasonDialog: MatDialog) {
  }

  ngAfterViewInit(): void {
    this.certificateService.getAll().subscribe((res) => {
      this.certificates = res;
      this.dataSource = new MatTableDataSource<CertificateDetailsWithUserInfo>(this.certificates);
      this.dataSource.paginator = this.paginator;
      this.dataSource.sortingDataAccessor = (item, property) => {
        switch (property) {
          case 'owner.fullName':
            return item.owner.fullName;
          case 'organizationData':
            return item.organizationData.name + ', ' + item.organizationData.unit + ', ' + item.organizationData.countryCode;
          default: // @ts-ignore
            return item[property];
        }
      };
      this.dataSource.sort = this.sort;
    });
  }

  public validate(): void {
    this.certificateService.validate(this.selectedCertificate.serialNumber).subscribe(
      result => {
        if (result) {
          this.notificationService.createNotification('Certificate is valid!');
        } else {
          this.notificationService.createNotification('Certificate is not valid!');
        }
      }
    )
  }

  public download(): void {
    this.certificateService.download(this.selectedCertificate.serialNumber).subscribe(
      result => {
        const file = new Blob([result], {type: 'application/octet-stream'});
        const fileReader = new FileReader();
        fileReader.onload = () => {
          const url = URL.createObjectURL(file);
          const a = document.createElement('a');
          a.href = url;
          a.download = 'certificate' + this.selectedCertificate.serialNumber + '.crt';
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
          URL.revokeObjectURL(url);
        };
        fileReader.readAsArrayBuffer(file);
      }
    );
  }

  public  withdraw() : void {
    const dialogRef = this.withdrawalReasonDialog.open(WithdrawReasonDialogComponent, {
      data: this.withdrawalReason,
    });

    dialogRef.afterClosed().subscribe(result => {
      if(result == undefined || result.denialReason == '') return;
      this.withdrawalReason = result;
      this.userCertificateService.withdrawCertificate(this.withdrawalReason,this.selectedCertificate.serialNumber.toString()).subscribe(
        result => {
          window.location.reload();
        },(err:HttpErrorResponse)=>{this.notificationService.createNotification(err.error);}
      );
    });
  }
  public refreshUI(certificate: CertificateDetailsWithUserInfo, index: number): void {
    this.cindex = index;
    if (!certificate) {
      this.enableStatusChange = false;
      this.enableWithdrawChange = false;
      return;
    }
    this.selectedCertificate = certificate;
    this.enableStatusChange = true;
    if(this.userService.getRole() == 'ROLE_ADMIN' || (this.userService.getRole() != 'ROLE_ADMIN' && this.selectedCertificate.certificateType != CertificateType.ROOT)) {
      this.enableWithdrawChange = true;
    }
    else {
      this.enableWithdrawChange = false;
    }
  }

  files: NgxFileDropEntry[] = [];

  onFileDropped(files: NgxFileDropEntry[]) {
    this.files = files;
    for (const droppedFile of files) {
      if (droppedFile.fileEntry.isFile) {
        const fileEntry = droppedFile.fileEntry as FileSystemFileEntry;
        fileEntry.file((file: File) => {
          if (file.type != "application/x-x509-ca-cert") {
            alert('Please drop only certificate files. ' + file.name + ' is not a certificate file.')
            return;
          }
          if (file.size > 1000000) {
            alert('File ' + file.name + ' is too big. Max size is 1MB.')
            return;
          }
          const formData = new FormData()
          formData.append('file', file, droppedFile.relativePath)

          this.certificateService.validateFormData(formData).subscribe({
              next: response => {
                if (response) {
                  alert(file.name + ' is a VALID certificate file.');
                } else {
                  alert(file.name + ' is NOT a valid certificate file.');
                }
              },
              error: error => {
                alert(file.name + ' is NOT a valid certificate file.');
              }
          });
        });
      } else {
        alert('Please drop only certificate files. ' + droppedFile.relativePath + ' is not a certificate file.');
      }
    }
  }
}
