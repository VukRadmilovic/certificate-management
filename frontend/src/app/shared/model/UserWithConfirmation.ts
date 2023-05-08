import {User} from "./User";

export interface UserWithConfirmation extends User{
  confirmation: string
}
