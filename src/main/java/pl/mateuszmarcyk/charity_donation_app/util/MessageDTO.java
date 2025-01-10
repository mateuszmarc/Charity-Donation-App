package pl.mateuszmarcyk.charity_donation_app.util;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {

    @NotNull(message = "{messageDTO.firstname.notnull}")
    private String firstName;

    @NotNull(message = "{messageDTO.lastname.notnull}")
    private String lastName;

    @NotNull(message = "{messageDTO.message.notnull}")
    private String message;

    @Email(message = "{messageDTO.email.notnull}")
    @NotNull(message = "{messageDTO.email.email}")
    private String email;
}
