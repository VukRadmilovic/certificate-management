import {AfterViewInit, Component, ViewChild} from '@angular/core';
import {RequestDenyReasonDialogComponent} from "../request-deny-reason-dialog/request-deny-reason-dialog.component";
import {RequestStatus} from "../../shared/model/enums/RequestStatus";
import {NotificationsService} from "../../shared/notifications.service";
import {UserRequestsService} from "../services/user-requests.service";
import {MatDialog} from "@angular/material/dialog";
import {CertificateRequestDetails} from "../../shared/model/CertificateRequestDetails";
import {DenialReason} from "../../shared/model/DenialReason";
import {MatTableDataSource} from "@angular/material/table";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";

@Component({
  selector: 'app-received-requests',
  templateUrl: './received-requests.component.html',
  styleUrls: ['./received-requests.component.css']
})
export class ReceivedRequestsComponent implements AfterViewInit{

  displayedColumns: string[] = ['issuerSerialNumber', 'certificateType','requester.fullName', 'organizationData', 'dateRequested', 'validUntil' , 'requestStatus', 'denialReason'];
  enableStatusChange: boolean = false;
  dataSource!: MatTableDataSource<CertificateRequestDetails>;
  requests: CertificateRequestDetails[] = [];
  selectedRequest!: CertificateRequestDetails;
  denialReason: DenialReason = {
    denialReason:''
  };

  @ViewChild(MatPaginator) paginator!: any;
  @ViewChild(MatSort) sort!: any;
  cindex: number = -1;

  constructor(private notificationService: NotificationsService,
              private userRequestsService: UserRequestsService,
              public denyReasonDialog: MatDialog) {
  }
  ngAfterViewInit() : void {
    this.userRequestsService.getReceived().subscribe((res) => {
      this.requests = res;
      this.dataSource = new MatTableDataSource<CertificateRequestDetails>(this.requests);
      this.dataSource.paginator = this.paginator;
      this.dataSource.sortingDataAccessor = (item, property) => {
        switch(property) {
          case 'requester.fullName': return item.requester.fullName;
          case 'organizationData': return item.organizationData.name + ', ' + item.organizationData.unit + ', ' + item.organizationData.countryCode
          default: // @ts-ignore
            return item[property];
        }
      };
      this.dataSource.sort = this.sort;
    });
  }
  public acceptRequest() : void {
    this.userRequestsService.acceptRequest(this.selectedRequest.id.toString()).subscribe(
      result => {
        this.selectedRequest.requestStatus = RequestStatus.ACCEPTED;
        this.notificationService.createNotification('Request successfully accepted!');
      }
    );
  }

  public  denyRequest() : void {
    const dialogRef = this.denyReasonDialog.open(RequestDenyReasonDialogComponent, {
      data: this.denialReason,
    });

    dialogRef.afterClosed().subscribe(result => {
      if(result == undefined || result.denialReason == '') return;
      this.denialReason = result;
      this.userRequestsService.denyRequest(this.denialReason,this.selectedRequest.id.toString()).subscribe(
        result => {
          this.selectedRequest.requestStatus = RequestStatus.DENIED;
          this.selectedRequest.denialReason = this.denialReason.denialReason;
          this.notificationService.createNotification('Request successfully denied!');
        }
      );
    });
  }

  public refreshUI(request: CertificateRequestDetails, index: number) : void {
    this.cindex = index;
    if(request.requestStatus != RequestStatus.PENDING && request.requestStatus != RequestStatus.WITHDRAWN)
    {
      this.enableStatusChange = false;
      return;
    }
    this.selectedRequest = request;
    this.enableStatusChange = true;
  }
}
