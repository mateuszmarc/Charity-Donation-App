package pl.mateuszmarcyk.charity_donation_app;

public class ErrorMessages {
    public static final String ACCOUNT_DISABLED = "/app/login?error=Your account is not enabled.";
    public static final String ACCOUNT_BLOCKED = "/app/login?error=Your account is blocked.";
    public static final String INVALID_CREDENTIALS = "/app/login?error=Invalid username or password.";
    public static final String USERNAME_NOT_FOUND_EXCEPTION_MESSAGE = "Could not find the user";
    public static final String DISABLED_EXCEPTION_MESSAGE = "User is not enabled";
    public static final String LOCKED_EXCEPTION_MESSAGE = "User is blocked";
    public static final String ILLEGAL_STATE_EXCEPTION_MESSAGE = "Authenticated user has no roles assigned.";
    public static final String USER_NOT_FOUND_EXCEPTION_TITLE = "Brak użytkownika";
    public static final String USER_NOT_FOUND_EXCEPTION_MESSAGE = "Użytkownik nie istnieje";
    public static final String ACCESS_DENIED_EXCEPTION_TITLE = "Odmowa dostępu";
    public static final String ACCESS_DENIED_EXCEPTION_MESSAGE = "Nie masz uprawnień aby wejść na stronę";
    public static final String PAGE_DOES_NOT_EXIST_TITLE = "Ooops.... Mamy problem";
    public static final String PAGE_DOES_NOT_EXIST_MESSAGE = "Taka strona nie istnieje";
    public static final String UNKNOWN_ERROR_TITLE = "Wystąpił błąd";
    public static final String UNKNOWN_ERROR_MESSAGE = "Nieznany błąd serwera";
    public static final String MAIL_EXCEPTION_TITLE = "Nie można wysłać";
    public static final String MAIL_EXCEPTION_MESSAGE = "Wystąpił błąd podczas wysyłania. Spróbuj ponownie";
    public static final String TOKEN_NOT_FOUND_EXCEPTION_TITLE = "Token nie znaleziony";
    public static final String TOKEN_NOT_FOUND_EXCEPTION_MESSAGE = "Link jest uszkodzony";
}