package pl.mateuszmarcyk.charity_donation_app.util.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import pl.mateuszmarcyk.charity_donation_app.entity.VerificationToken;
import pl.mateuszmarcyk.charity_donation_app.entity.User;

@Getter
@Setter
public class ResendTokenEvent extends ApplicationEvent {

    private User user;
    private String applicationUrl;
    private VerificationToken oldToken;

    public ResendTokenEvent(User user, String applicationUrl, VerificationToken oldToken) {
        super(user);
        this.user = user;
        this.applicationUrl = applicationUrl;
        this.oldToken = oldToken;
    }
}
