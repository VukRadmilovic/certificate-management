import {AfterViewInit, Component, ViewChild} from '@angular/core';
import {MatTableDataSource} from "@angular/material/table";
import {CertificateRequestDetails} from "../../shared/model/CertificateRequestDetails";
import {DenialReason} from "../../shared/model/DenialReason";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {NotificationsService} from "../../shared/notifications.service";
import {UserRequestsService} from "../../user/services/user-requests.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-all-requests',
  templateUrl: './all-requests.component.html',
  styleUrls: ['./all-requests.component.css']
})
export class AllRequestsComponent implements AfterViewInit{
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

  constructor(private notificationService: NotificationsService,
              private userRequestsService: UserRequestsService,
              private router: Router) {}
  ngAfterViewInit() : void {
    this.userRequestsService.getAll().subscribe((res) => {
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
  public newRequest() : void {
    this.router.navigate(['new-request']);
  }
}
