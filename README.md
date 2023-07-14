# Certificate Management Web Application

This project is a web application developed as part of the Information Security course. It allows users to work with certificates and perform various operations such as certificate issuance, revocation, validation, and more. The application is built using a combination of Spring Boot for the backend and Angular for the frontend.

## Tech Stack
- Backend: Spring Boot
- Frontend: Angular
- Database: PostgreSQL
- Keystore: Java KeyStore (JKS)
- Email Service: SendGrid
- Two-Factor Authentication: WhatsApp and Email
- OAuth Integration: Google

## Features
- User Management:
  - Registration and login with email, password and 2FA
  - Password recovery with verification code using email or phone number
  - Users are required to periodically renew their passwords
  - No reuse of previous passwords

- Certificate Management:
  - View, download, and validate certificates
  - Support for revoked and expired certificates
  - Validate uploaded or already existing certificates
  - Users can request new certificates (intermediate or end)
  - Admin can request root certificates
  - Users can view their active and past certificate requests
  - Admin can view all active and past certificate requests
  - Certificate owners can approve or reject pending requests
  - Admin approval is required for new root certificates
  - Certificate owners can revoke their certificates at any time
  - Revoking a certificate automatically revokes all certificates signed by it, creating a chain of revocations
  - The admin can revoke any certificate, including root certificates

- HTTPS Communication:
  - The application uses a root certificate managed by the admin for secure communication
  - The database connection uses a separate certificate to enable encrypted communication between the database and the backend

- Form Protection with ReCAPTCHA:
  - Forms are protected against spamming using ReCAPTCHA mechanisms
  - Token validation is implemented on both the client and server sides

- OAuth Integration:
  - Users can log in to the system using delegated access via OAuth protocols (Google OAuth)
  - If a user already has a registered account, logging in through OAuth links the accounts instead of creating a new one

## How to Run
1. Clone the repository.
2. Navigate to the backend directory.
3. Install the necessary dependencies:
```
mvn install
```
4. Run the backend:
```
mvn spring-boot:run
```
The backend server should now be running on http://localhost:8080.
5. Navigate to the frontend directory
6. Install frontend dependencies:
```
npm install
```
7. Start the frontend development server:
```
ng serve
```
Open your web browser and visit http://localhost:4200 to access the application.
