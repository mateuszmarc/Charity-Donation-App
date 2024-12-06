package pl.mateuszmarcyk.charity_donation_app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "institutions")
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "{institution.name.notnull}")
    @Column(name = "name")
    private String name;

    @NotNull(message = "{institution.description.notnull}")
    @Column(name = "description")
    private String description;

    @ToString.Exclude
    @OneToMany(
            targetEntity = Donation.class,
            mappedBy = "institution",
            cascade = {
                    CascadeType.DETACH,
                    CascadeType.MERGE,
                    CascadeType.PERSIST,
                    CascadeType.REFRESH
            },
            fetch = FetchType.LAZY
    )
    private List<Donation> donations;
}
