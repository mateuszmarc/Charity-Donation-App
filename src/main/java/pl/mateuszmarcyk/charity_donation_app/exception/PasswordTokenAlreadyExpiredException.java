package pl.mateuszmarcyk.charity_donation_app.exception;

import lombok.Getter;

@Getter
public class PasswordTokenAlreadyExpiredException extends RuntimeException implements BusinessException {
    private final String title;

    public PasswordTokenAlreadyExpiredException(String message, String title) {
        super(message);
        this.title = title;
    }
}
