package pl.mateuszmarcyk.charity_donation_app.usertype;

import jakarta.persistence.*;
import lombok.*;
import pl.mateuszmarcyk.charity_donation_app.user.User;

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

    @ManyToMany(targetEntity = User.class,
            cascade = {
                    CascadeType.DETACH,
                    CascadeType.MERGE,
                    CascadeType.PERSIST,
                    CascadeType.REFRESH
            },
            fetch = FetchType.LAZY
    )
    @JoinTable(name = "users_user_types",
            joinColumns = @JoinColumn(name = "user_type_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> users;

    public void addUser(User user) {
        users.add(user);
    }
}
