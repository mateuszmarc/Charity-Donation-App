package pl.mateuszmarcyk.charity_donation_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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

    public void removeUser(User user) {
        users.removeIf(element -> element.getId().equals(user.getId()));
    }

    @Override
    public String toString() {
        return role;
    }
}
