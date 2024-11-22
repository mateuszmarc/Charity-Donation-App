package pl.mateuszmarcyk.charity_donation_app.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Mail {
    private String Subject;
    private String senderName;
    private String mailContent;
}
