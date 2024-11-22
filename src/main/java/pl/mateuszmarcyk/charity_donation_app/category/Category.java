package pl.mateuszmarcyk.charity_donation_app.category;

import jakarta.persistence.*;
import lombok.*;
import pl.mateuszmarcyk.charity_donation_app.donation.Donation;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @ToString.Exclude
    @ManyToMany(
            targetEntity = Donation.class,
            mappedBy = "categories",
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
