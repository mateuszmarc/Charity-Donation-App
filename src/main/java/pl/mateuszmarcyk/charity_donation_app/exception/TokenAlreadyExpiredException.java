package pl.mateuszmarcyk.charity_donation_app.exception;

import lombok.Getter;

@Getter
public class TokenAlreadyExpiredException extends RuntimeException implements BusinessException {
    private final String title;
    private final String token;

    public TokenAlreadyExpiredException(String title, String message,  String token) {
        super(message);
        this.title = title;
        this.token = token;
    }
}
