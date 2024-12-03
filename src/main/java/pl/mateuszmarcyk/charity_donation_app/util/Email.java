package pl.mateuszmarcyk.charity_donation_app.util;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Email {

    @NotNull(message = "{email.addressemail.notnull}")
    @jakarta.validation.constraints.Email(message = "{email.addressemail.email}")
    @UserEmail(message = "{email.addressemail.useremail}")
    private String addressEmail;
}
