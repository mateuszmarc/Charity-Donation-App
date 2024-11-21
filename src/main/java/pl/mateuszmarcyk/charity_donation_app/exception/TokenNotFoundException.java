package pl.mateuszmarcyk.charity_donation_app.exception;

import lombok.Getter;

@Getter
public class TokenNotFoundException extends RuntimeException implements BusinessException{
    private String title;

    public TokenNotFoundException(String title, String message) {
        super(message);
        this.title = title;
    }
}
