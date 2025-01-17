package pl.mateuszmarcyk.charity_donation_app.exception;

import lombok.Getter;

@Getter
public class TokenAlreadyConsumedException extends RuntimeException implements BusinessException{
    private final String title;

    public TokenAlreadyConsumedException(String title, String message) {
        super(message);
        this.title = title;
    }
}
