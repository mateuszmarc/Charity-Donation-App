package pl.mateuszmarcyk.charity_donation_app.donation;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import pl.mateuszmarcyk.charity_donation_app.category.Category;
import pl.mateuszmarcyk.charity_donation_app.institution.Institution;
import pl.mateuszmarcyk.charity_donation_app.user.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name= "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "${donation.quantity.notnull}")
    @Min(value = 1, message = "{donation.quantity.min}")
    @Column(name = "quantity")
    private Integer quantity;

    @NotNull(message = "{donation.street.notnull}")
    @Column(name = "street")
    private String street;

    @NotNull(message = "{donation.city.notnull}")
    @Column(name = "city")
    private String city;

    @NotNull(message = "{donation.zipCode.notnull}")
    @Pattern(regexp = "[0-9]{2}-[0-9]{3}", message = "{donation.zipCode.pattern}")
    @Column(name = "zip_code")
    private String zipCode;

    @Future(message = "{donation.pickUpDate.future}")
    @Column(name = "pick_up_date")
    private LocalDate pickUpDate;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "{donation.pickUpTime.pattern}")
    @Column(name = "pick_up_time")
    private LocalTime pickUpTime;

    @Column(name = "pick_up_comment")
    private String pickUpComment;

    @NotNull
    @Pattern(regexp = "[0-9]{9}", message = "{donation.phoneNumber.pattern}")
    @Column(name = "phone_number")
    private String phoneNumber;

    @ManyToMany(
            targetEntity = Category.class,
            cascade = {
                    CascadeType.DETACH,
                    CascadeType.MERGE,
                    CascadeType.PERSIST,
                    CascadeType.REFRESH
            },
            fetch = FetchType.LAZY
    )
    @JoinTable(name = "donations_categories",
            joinColumns = @JoinColumn(name = "donation_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;

    @ManyToOne(targetEntity = Institution.class,
            cascade = {
                    CascadeType.DETACH,
                    CascadeType.MERGE,
                    CascadeType.PERSIST,
                    CascadeType.REFRESH
            }
    )
    @JoinColumn(name = "institution_id")
    private Institution institution;

    @ManyToOne(
            targetEntity = User.class,
            cascade = {
                    CascadeType.DETACH,
                    CascadeType.MERGE,
                    CascadeType.PERSIST,
                    CascadeType.REFRESH
            }
    )
    @JoinColumn(name = "user_id")
    private User user;
}
