import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {WithdrawalReason} from "../../shared/model/WithdrawalReason";

@Component({
  selector: 'app-withdraw-reason-dialog',
  templateUrl: './withdraw-reason-dialog.component.html',
  styleUrls: ['./withdraw-reason-dialog.component.css']
})
export class WithdrawReasonDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<WithdrawReasonDialogComponent>,
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
