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

    public VerificationToken(String token, User user, int tokenValidTimeMinutes) {
        this.token = token;
        this.expirationTime = getTokenExpirationTime(tokenValidTimeMinutes);
        this.user = user;
    }

    private LocalDateTime getTokenExpirationTime(int tokenValidTimeMinutes) {

        LocalDateTime currentTime = LocalDateTime.now();
        return currentTime.plusMinutes(tokenValidTimeMinutes);
    }
}
