package pl.mateuszmarcyk.charity_donation_app.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException implements BusinessException{
    private final String title;

    public ResourceNotFoundException(String title, String message) {
        super(message);
        this.title = title;
    }
}
