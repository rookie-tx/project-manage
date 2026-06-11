## MODIFIED Requirements

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
