package pl.mateuszmarcyk.charity_donation_app.user;

import jakarta.persistence.*;
import lombok.*;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.VerificationToken;
import pl.mateuszmarcyk.charity_donation_app.usertype.UserType;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "is_active")
    private boolean enabled;

    @Column(name = "password")
    private String password;

    @Column(name = "registration_date_time")
    private LocalDateTime registrationDate;

    @ManyToMany(
            targetEntity = UserType.class,
            cascade = {
                    CascadeType.DETACH,
                    CascadeType.MERGE,
                    CascadeType.PERSIST,
                    CascadeType.REFRESH
            },
            fetch = FetchType.LAZY
    )
    @JoinTable(name = "users_user_types",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "user_type_id"))
    private List<UserType> userTypes;

    @OneToOne(
            targetEntity = UserProfile.class,
            cascade = CascadeType.ALL,
            mappedBy = "user")
    private UserProfile profile;

    @OneToOne(
            targetEntity = VerificationToken.class,
            cascade = CascadeType.ALL
    )
    private VerificationToken verificationToken;

    public void setUserProfile(UserProfile userProfile) {
        this.profile = userProfile;
        userProfile.setUser(this);
    }

    public void grantAuthority(UserType userType) {

       boolean hasAlreadyThisRole = userTypes.stream().anyMatch(type -> type.getId().equals(userType.getId()));
        if (!hasAlreadyThisRole) {
            userTypes.add(userType);
            userType.addUser(this);
        }
    }
}
