import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DenialReason} from "../../shared/model/DenialReason";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {UserRequestsService} from "../services/user-requests.service";

@Component({
  selector: 'app-request-deny-reason-dialog',
  templateUrl: './request-deny-reason-dialog.component.html',
  styleUrls: ['./request-deny-reason-dialog.component.css']
})

export class RequestDenyReasonDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<RequestDenyReasonDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DenialReason) {}

  reasonForm = new FormGroup({
    denialReason: new FormControl( '',[Validators.required]),
  });

  close(): void {
    this.dialogRef.close();
  }
  submit() : void {
    if(this.reasonForm.valid){
      this.data = {
        denialReason : <string>this.reasonForm.value.denialReason
      }
      this.dialogRef.close(this.data);
    }
  }
}
