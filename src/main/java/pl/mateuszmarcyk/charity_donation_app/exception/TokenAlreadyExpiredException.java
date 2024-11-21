package pl.mateuszmarcyk.charity_donation_app.exception;

import lombok.Getter;

@Getter
public class TokenAlreadyExpiredException extends RuntimeException implements BusinessException {
    private String title;

    public TokenAlreadyExpiredException(String message, String title) {
        super(message);
        this.title = title;
    }
}
