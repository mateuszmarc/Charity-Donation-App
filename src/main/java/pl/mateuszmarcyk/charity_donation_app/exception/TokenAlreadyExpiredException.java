package pl.mateuszmarcyk.charity_donation_app.exception;

import lombok.Getter;

@Getter
public class TokenAlreadyExpiredException extends RuntimeException implements BusinessException {
    private String title;
    private String token;

    public TokenAlreadyExpiredException(String message, String title, String token) {
        super(message);
        this.title = title;
        this.token = token;
    }
}
