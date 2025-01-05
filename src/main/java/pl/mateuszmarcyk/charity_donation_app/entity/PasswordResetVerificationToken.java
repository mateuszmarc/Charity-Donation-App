package pl.mateuszmarcyk.charity_donation_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "password_reset_verification_tokens")
public class PasswordResetVerificationToken {

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

    @Transient
    private LocalDateTime created;

    @Column(name = "consumed")
    private boolean consumed = false;

    public PasswordResetVerificationToken(String token, User user, int tokenValidTimeMinutes) {
        this.token = token;
        this.created = LocalDateTime.now();
        this.expirationTime = getTokenExpirationTime(created, tokenValidTimeMinutes);
        this.user = user;
    }

    private LocalDateTime getTokenExpirationTime(LocalDateTime created, int tokenValidTimeMinutes) {

        return created.plusMinutes(tokenValidTimeMinutes);
    }

}
