# Account authentication policy

## Logout

- The service issues access tokens only. It does not issue or store refresh tokens.
- Logout removes `matcheat.accessToken` from browser `sessionStorage` and stops adding the Authorization header.
- A normal logout cannot revoke an already copied access token on the server. The default access-token lifetime is one hour and can be changed with `JWT_ACCESS_TOKEN_TTL`.
- Account suspension, withdrawal, and seller-role promotion increment `token_version`. The JWT decoder compares the token claim with the current account value, so tokens issued before those events are rejected immediately.

## Withdrawal

- A member cannot withdraw while an order request is `MATCHING`, `IN_TALK`, or `CONFIRMED`.
- A member cannot withdraw while a quote is `SENT` or `ACCEPTED`, or while a quote negotiation exists (the current model has no post-lock settlement/completion state).
- A seller also cannot withdraw while a proposal is `SENT`, `IN_TALK`, or `ACCEPTED`, or an estimate is `REQUESTED` or `ACCEPTED`.
- A blocked request returns HTTP 409 with `ACTIVE_TRANSACTION_EXISTS`.
- Withdrawal is soft deletion: status becomes `WITHDRAWN`, `withdrawn_at` is recorded, and `token_version` is incremented. Transaction rows remain intact for referential integrity.
- The email remains reserved because the current schema keeps its unique constraint. Re-registration with the same email is not allowed.

## Social accounts

Social signup, login, and account linking are intentionally deferred. No provider identifiers or OAuth credentials are stored by this release.
