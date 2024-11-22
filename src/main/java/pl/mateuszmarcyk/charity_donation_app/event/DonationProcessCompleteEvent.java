package pl.mateuszmarcyk.charity_donation_app.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import pl.mateuszmarcyk.charity_donation_app.donation.Donation;
import pl.mateuszmarcyk.charity_donation_app.user.User;

@Getter
@Setter
public class DonationProcessCompleteEvent extends ApplicationEvent {

    private Donation donation;
    private User user;
    public DonationProcessCompleteEvent(Donation donation, User user) {
        super(donation);
        this.donation = donation;
        this.user = user;
    }
}
