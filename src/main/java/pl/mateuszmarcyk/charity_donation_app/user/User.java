package pl.mateuszmarcyk.charity_donation_app.user;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import pl.mateuszmarcyk.charity_donation_app.donation.Donation;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.PasswordResetVerificationToken;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.VerificationToken;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.usertype.UserType;
import pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.PasswordEqual;
import pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.UniqueEmail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@PasswordEqual
@UniqueEmail
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

    @NotNull(message= "{user.email.notnull}")
    @Email(message = "{user.email.email}")
    @Column(name = "email")
    private String email;

    @Column(name = "is_active")
    private boolean enabled = false;

    @Column(name = "blocked")
    private boolean blocked = false;

    @NotNull(message = "{user.password.notnull}")
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*\\W)_*.{6,}", message = "{user.password.pattern}")
    @Column(name = "password")
    private String password;

    @Column(name = "registration_date_time")
    private LocalDateTime registrationDate;

    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*\\W)_*.{6,}", message = "{user.password.pattern}")
    @Transient
    private String passwordRepeat;

    @ToString.Exclude
    @ManyToMany(
            targetEntity = UserType.class,
            cascade = {
                    CascadeType.DETACH,
                    CascadeType.MERGE,
                    CascadeType.PERSIST,
                    CascadeType.REFRESH
            },
            fetch = FetchType.EAGER
    )
    @JoinTable(name = "users_user_types",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "user_type_id"))
    private List<UserType> userTypes = new ArrayList<>();

    @ToString.Exclude
    @Valid
    @OneToOne(
            targetEntity = UserProfile.class,
            cascade = CascadeType.ALL,
            mappedBy = "user")
    private UserProfile profile;

    @OneToOne(
            targetEntity = VerificationToken.class,
            mappedBy = "user",
            cascade = CascadeType.ALL
    )
    private VerificationToken verificationToken;

    @OneToOne(
            targetEntity = PasswordResetVerificationToken.class,
            mappedBy = "user",
            cascade = CascadeType.ALL
    )
    private PasswordResetVerificationToken passwordResetVerificationToken;

    @OneToMany(
            mappedBy = "user",
            cascade = {
                    CascadeType.DETACH,
                    CascadeType.MERGE,
                    CascadeType.PERSIST,
                    CascadeType.REFRESH
            },
            fetch = FetchType.LAZY
    )
    private List<Donation> donations;

    public void setUserProfile(UserProfile userProfile) {
        this.profile = userProfile;
        userProfile.setUser(this);
    }

    public void grantAuthority(UserType userType) {

       boolean hasAlreadyThisRole = userTypes.stream().anyMatch(type -> type.getId().equals(userType.getId()));
        if (!hasAlreadyThisRole) {
            userTypes.add(userType);
        }
    }

    @PrePersist
    public void prePersist() {
        this.registrationDate = LocalDateTime.now();
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
        profile.setUser(this);
    }

    public void addUserType(UserType userType) {
        if (userTypes.stream().noneMatch(type -> type.getId().equals(userType.getId()))) {
            userTypes.add(userType);
        }
    }

    public void removeUserType(UserType userType) {
        this.getUserTypes().removeIf(type -> type.getId().equals(userType.getId()));
    }
}
