package pl.mateuszmarcyk.charity_donation_app.usertype;

import jakarta.persistence.*;
import lombok.*;
import pl.mateuszmarcyk.charity_donation_app.user.User;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "user_types")
public class UserType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "role")
    private String role;

    @ToString.Exclude
    @ManyToMany(targetEntity = User.class,
            mappedBy = "userTypes",
            cascade = {
                    CascadeType.DETACH,
                    CascadeType.MERGE,
                    CascadeType.PERSIST,
                    CascadeType.REFRESH
            },
            fetch = FetchType.LAZY
    )
    private List<User> users = new ArrayList<>();

    public void addUser(User user) {
        users.add(user);
    }
}
