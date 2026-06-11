## ADDED Requirements

### Requirement: User can login with valid credentials
The system SHALL authenticate a user by username and password, and upon success create an HTTP session containing the user's identity.

#### Scenario: Successful login
- **WHEN** user sends POST `/api/auth/login` with valid `username` and `password`
- **THEN** system returns 200 with `LoginResponse` containing `userId` and `username`
- **AND** response includes `Set-Cookie` header with `JSESSIONID`

#### Scenario: Invalid credentials
- **WHEN** user sends POST `/api/auth/login` with incorrect `username` or `password`
- **THEN** system returns 401 with error message "Invalid credentials"

#### Scenario: Account disabled
- **WHEN** user sends POST `/api/auth/login` with valid credentials but account status is disabled
- **THEN** system returns 403 with error message "Account disabled"

### Requirement: User can check current login status
The system SHALL return the current authenticated user's identity if a valid session exists.

#### Scenario: Authenticated session
- **WHEN** user sends GET `/api/auth/me` with a valid `JSESSIONID` cookie
- **THEN** system returns 200 with `LoginResponse` containing `userId` and `username`

#### Scenario: No valid session
- **WHEN** user sends GET `/api/auth/me` without a valid session
- **THEN** system returns 401 with error message "Not authenticated"

### Requirement: User can logout
The system SHALL destroy the current HTTP session upon logout.

#### Scenario: Successful logout
- **WHEN** user sends POST `/api/auth/logout` with a valid session
- **THEN** system returns 200 with success message
- **AND** the session is invalidated on the server

#### Scenario: Logout without session
- **WHEN** user sends POST `/api/auth/logout` without a valid session
- **THEN** system returns 200 (idempotent)

### Requirement: Password is hashed with BCrypt
The system SHALL use BCrypt for password hashing and verification. Raw passwords MUST never be stored.

#### Scenario: Password verification
- **WHEN** login credentials are submitted
- **THEN** the raw password is compared against the stored BCrypt hash using the `PasswordHasher` interface
- **AND** the raw password is never persisted or logged
