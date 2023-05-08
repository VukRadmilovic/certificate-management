import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {UserWithConfirmation} from "../model/UserWithConfirmation";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {UserService} from "../user.service";
import {Router} from "@angular/router";
import {NotificationsService} from "../notifications.service";

export interface DialogData {
  loginRegisterPassword: number;
  name: string;
  surname: string;
  phoneNumber: string;
  email: string;
  password: string;
  confirmation: string;
  go: boolean;
}

@Component({
  selector: 'confirm-dialog',
  templateUrl: '../confirm-dialog/confirm-dialog.html',
})
export class ConfirmDialog {
  constructor(
    public dialogRef: MatDialogRef<ConfirmDialog>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
    private userService : UserService,
    private router: Router,
    private notificationService: NotificationsService,
    public dialog: MatDialog
  ) {}
  onNoClick(): void {
    this.dialogRef.close();
  }
  onConfirmClick(): void{
    //if (this.confirmationForm.valid) {
      if (this.data.loginRegisterPassword == 1){

      }
      else if (this.data.loginRegisterPassword == 2) {
        const user: UserWithConfirmation = {
          name: this.data.name,
          surname: this.data.surname,
          phoneNumber: this.data.phoneNumber,
          email: this.data.email,
          password: this.data.password,
          confirmation: this.data.confirmation,
        }
        this.userService.confirm(user).subscribe({
          next: () => {
            this.notificationService.createNotification("User successfully confirmed!");
          },
          error: (error) => {
            this.notificationService.createNotification(error.error);
          }
        });
      }
      else if (this.data.loginRegisterPassword == 3){

      }
    }
  //}
}
