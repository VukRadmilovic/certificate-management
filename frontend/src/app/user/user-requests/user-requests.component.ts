import {Component, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from "@angular/material/table";
import {CertificateRequestDetails} from "../../shared/model/CertificateRequestDetails";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {UserRequestsService} from "../services/user-requests.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-user-requests',
  templateUrl: './user-requests.component.html',
  styleUrls: ['./user-requests.component.css']
})
export class UserRequestsComponent implements OnInit {
  displayedColumns: string[] = ['issuerSerialNumber', 'certificateType', 'organizationData', 'dateRequested', 'validUntil' , 'requestStatus', 'denialReason'];
  dataSource!: MatTableDataSource<CertificateRequestDetails>;
  requests: CertificateRequestDetails[] = [];

  constructor(private userRequestsService: UserRequestsService, private router: Router) {}

  @ViewChild(MatPaginator) paginator!: any;
  @ViewChild(MatSort) sort!: any;

  ngOnInit() : void {
    this.userRequestsService.getAll().subscribe((res) => {
      this.requests = res;
      this.dataSource = new MatTableDataSource<CertificateRequestDetails>(this.requests);
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

  public newRequest() : void {
    this.router.navigate(['new-request']);
  }

}

