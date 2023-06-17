import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {WithdrawalReason} from "../model/WithdrawalReason";

@Component({
  selector: 'app-captcha-dialog',
  templateUrl: './captcha-dialog.component.html',
  styleUrls: ['./captcha-dialog.component.css']
})
export class CapthcaDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<CapthcaDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: WithdrawalReason) {}

  reasonForm = new FormGroup({
    withdrawalReason: new FormControl( '',[Validators.required]),
  });

  close(): void {
    this.dialogRef.close();
  }
  submit() : void {
    if(this.reasonForm.valid){
      this.data = {
        reason : <string>this.reasonForm.value.withdrawalReason
      }
      this.dialogRef.close(this.data);
    }
  }
}
