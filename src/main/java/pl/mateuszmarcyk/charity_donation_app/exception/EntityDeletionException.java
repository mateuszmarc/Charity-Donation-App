package pl.mateuszmarcyk.charity_donation_app.exception;

public class EntityDeletionException extends RuntimeException implements BusinessException {
    private final String title;

    public EntityDeletionException(String title, String message) {
        super(message);
        this.title = title;
    }

    @Override
    public String getTitle() {
        return this.title;
    }
}
