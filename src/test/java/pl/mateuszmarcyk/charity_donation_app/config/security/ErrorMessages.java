package pl.mateuszmarcyk.charity_donation_app.config.security;

public class ErrorMessages {
    public static final String ACCOUNT_DISABLED = "/app/login?error=Your account is not enabled.";
    public static final String ACCOUNT_BLOCKED = "/app/login?error=Your account is blocked.";
    public static final String INVALID_CREDENTIALS = "/app/login?error=Invalid username or password.";
    public static final String ACCESS_DENIED_URL = "/error/403";
}
