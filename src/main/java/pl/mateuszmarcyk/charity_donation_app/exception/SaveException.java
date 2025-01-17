package pl.mateuszmarcyk.charity_donation_app.exception;

public class SaveException extends RuntimeException implements BusinessException {
    private final String title;

    public SaveException(String message, String title) {
        super(message);
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
