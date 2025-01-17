package pl.mateuszmarcyk.charity_donation_app.exception;

public class MailException extends RuntimeException implements BusinessException {
    private final String title;

    public MailException(String message, String title) {
        super(message);
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

}
