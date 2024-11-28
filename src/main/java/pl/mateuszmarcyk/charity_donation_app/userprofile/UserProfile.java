package pl.mateuszmarcyk.charity_donation_app.userprofile;

import jakarta.persistence.*;
import lombok.*;
import pl.mateuszmarcyk.charity_donation_app.user.User;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ToString.Exclude
    @OneToOne(targetEntity = User.class, cascade = {
            CascadeType.DETACH,
            CascadeType.MERGE,
            CascadeType.PERSIST,
            CascadeType.REFRESH
    })
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "profile_photo", nullable = true, length = 64)
    private String profilePhoto;

    @Column(name = "phone_number")
    private String phoneNumber;

    public UserProfile(User user) {
        this.user = user;
    }

    @Transient
    public String getPhotosImagePath() {
        String photosPath = null;
        if (profilePhoto != null && id != null) {
            photosPath = "/photos/users/" + id + "/" + profilePhoto;
        }
        return photosPath;
    }
}
