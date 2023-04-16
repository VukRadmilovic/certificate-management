import {Component, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from "@angular/material/table";
import {CertificateRequestDetails} from "../../shared/model/CertificateRequestDetails";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {UserRequestsService} from "../services/user-requests.service";
import {RequestStatus} from "../../shared/model/enums/RequestStatus";
import {MatDialog} from "@angular/material/dialog";
import {RequestDenyReasonDialogComponent} from "../request-deny-reason-dialog/request-deny-reason-dialog.component";
import {DenialReason} from "../../shared/model/DenialReason";
import {NotificationsService} from "../../shared/notifications.service";

@Component({
  selector: 'app-user-requests',
  templateUrl: './user-requests.component.html',
  styleUrls: ['./user-requests.component.css']
})
export class UserRequestsComponent implements OnInit {
  displayedColumns: string[] = ['Issuer SN', 'Certificate Type', 'Organization', 'Date Requested', 'Valid (To)' , 'Status', 'Denial Reason'];
  dataSource!: MatTableDataSource<CertificateRequestDetails>;
  requests: CertificateRequestDetails[] = [];
  enableStatusChange: boolean = false;
  selectedRequest!: CertificateRequestDetails;
  denialReason: DenialReason = {
    denialReason:''
  };

  constructor(private userRequestsService: UserRequestsService,
              private notificationService: NotificationsService,
              public denyReasonDialog: MatDialog) {}

  @ViewChild(MatPaginator) paginator!: any;
  @ViewChild(MatSort) sort!: any;

  ngOnInit() : void {
    this.userRequestsService.getAll().subscribe((res) => {
      this.requests = res;
      this.dataSource = new MatTableDataSource<CertificateRequestDetails>(this.requests);
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
    });
  }
  public setButtonsStatus(request : CertificateRequestDetails) : void {
    if(request.requestStatus != RequestStatus.PENDING)
    {
      this.enableStatusChange = false;
      return;
    }
    this.selectedRequest = request;
    this.enableStatusChange = true;
  }

  public acceptRequest() : void {
    console.log("you can accept");
  }

  public denyRequest() : void {
    const dialogRef = this.denyReasonDialog.open(RequestDenyReasonDialogComponent, {
      data: this.denialReason,
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log(result)
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
}

