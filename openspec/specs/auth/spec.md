## Requirements

### Requirement: User can log in with username and password

The system SHALL allow a user to log in by submitting a username and password to `POST /api/auth/login`. On successful login, the browser's session cookie is set automatically, and the user is redirected to `/dashboard`.

#### Scenario: Successful login

- **WHEN** user submits valid username and password on `/login`
- **THEN** the system calls `POST /api/auth/login` with `{ username, password }` and `withCredentials: true`
- **AND** on 200 response, fetches user info via `GET /api/user/me`
- **AND** updates the auth store with the user object
- **AND** redirects to `/dashboard`

#### Scenario: Invalid credentials

- **WHEN** user submits incorrect username or password
- **THEN** the system receives HTTP 200 with `{ code: 1001, msg: "用户名或密码错误", data: null }` from the login endpoint
- **AND** the axios response interceptor rejects with an Error whose message is the server-provided msg
- **AND** the auth store sets `error` to the Error's message
- **AND** the login form displays the error message from the server
- **AND** the user remains on `/login`

### Requirement: User can log out

The system SHALL allow a logged-in user to log out by calling `POST /api/auth/logout`.

#### Scenario: Successful logout

- **WHEN** user clicks the logout button
- **THEN** the system calls `POST /api/auth/logout` with `withCredentials: true`
- **AND** clears the user state in the auth store
- **AND** redirects to `/login`

### Requirement: Session is restored on page refresh

The system SHALL restore the user session on page refresh by calling `GET /api/user/me`.

#### Scenario: Valid session exists

- **WHEN** the application loads and a valid session cookie exists
- **THEN** `GET /api/user/me` returns 200 with the user object
- **AND** the auth store is populated with the user object
- **AND** the user can access protected routes

#### Scenario: No valid session

- **WHEN** the application loads and no valid session cookie exists
- **THEN** `GET /api/user/me` returns 401
- **AND** the auth store `user` remains null
- **AND** accessing a protected route redirects to `/login`

### Requirement: Protected routes require authentication

The system SHALL prevent unauthenticated users from accessing protected routes by redirecting them to `/login`.

#### Scenario: Unauthenticated user accesses protected route

- **WHEN** an unauthenticated user navigates to `/dashboard`
- **THEN** the router guard redirects to `/login`

#### Scenario: Authenticated user accesses protected route

- **WHEN** an authenticated user navigates to `/dashboard`
- **THEN** the router guard allows navigation

#### Scenario: Authenticated user accesses login page

- **WHEN** an authenticated user navigates to `/login`
- **THEN** the router guard redirects to `/dashboard`

### Requirement: Login form displays validation and loading states

The system SHALL show loading state during login and display validation feedback on the form.

#### Scenario: Login in progress

- **WHEN** user submits the login form
- **THEN** the login button shows a loading spinner and is disabled
- **AND** the form fields are disabled during the request

#### Scenario: Login returns error

- **WHEN** the login request fails (network error or server-returned business error code)
- **THEN** an error message from the server is displayed to the user
- **AND** the form returns to editable state
