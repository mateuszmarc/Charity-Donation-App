package pl.mateuszmarcyk.charity_donation_app.exception;

public class TokenAlreadyConsumedException extends RuntimeException {
    public TokenAlreadyConsumedException(String message) {
        super(message);
    }
}
