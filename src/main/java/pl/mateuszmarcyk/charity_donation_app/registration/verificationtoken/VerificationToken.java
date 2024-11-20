package pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken;

import jakarta.persistence.*;
import pl.mateuszmarcyk.charity_donation_app.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_tokens")
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "token")
    private String token;

    @Column(name = "expiration_time")
    private LocalDateTime expirationTime;

    @OneToOne(targetEntity = User.class,
            cascade = {
                    CascadeType.DETACH,
                    CascadeType.MERGE,
                    CascadeType.PERSIST,
                    CascadeType.REFRESH
            }
    )
    @JoinColumn(name = "user_id")
    private User user;
}
