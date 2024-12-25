package pl.mateuszmarcyk.charity_donation_app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    @NotNull(message = "{donation.quantity.notnull}")
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

    @NotNull(message = "{donation.pickUpDate.notnull}")
    @Future(message = "{donation.pickUpDate.future}")
    @Column(name = "pick_up_date")
    private LocalDate pickUpDate;

    @NotNull(message = "{donation.pickUpTime.notnull}")
    @Column(name = "pick_up_time")
    private LocalTime pickUpTime;

    @Column(name = "pick_up_comment")
    private String pickUpComment;

    @NotNull
    @Pattern(regexp = "[0-9]{9}", message = "{donation.phoneNumber.pattern}")
    @Column(name = "phone_number")
    private String phoneNumber;

    @NotEmpty(message = "{donation.categories.notempty}")
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

    @NotNull(message = "{donation.institution.notnull}")
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

    @ToString.Exclude
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

    private LocalDateTime created;

    private boolean received = false;

    private LocalDateTime donationPassedTime;

    public Donation(LocalDateTime donationPassedTime, boolean received, User user, Institution institution, List<Category> categories, String phoneNumber, String pickUpComment, LocalTime pickUpTime, LocalDate pickUpDate, String zipCode, String city, String street, Integer quantity) {
        this.donationPassedTime = donationPassedTime;
        this.received = received;
        this.user = user;
        this.institution = institution;
        this.categories = categories;
        this.phoneNumber = phoneNumber;
        this.pickUpComment = pickUpComment;
        this.pickUpTime = pickUpTime;
        this.pickUpDate = pickUpDate;
        this.zipCode = zipCode;
        this.city = city;
        this.street = street;
        this.quantity = quantity;
    }

    public void removeCategory(Category category) {
        if (category != null) {
            categories.removeIf(cat -> cat.getId().equals(category.getId()));
        }
    }

    @PrePersist
    public void prePersist() {
        this.created = LocalDateTime.now();
    }

    public String getCreatedDateTime() {
        String datetimeToReturn = created.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(datetimeToReturn);
        return datetimeToReturn;
    }

    public String getDonationPassedDateTime() {
        return donationPassedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getCategoriesString() {
        if (categories == null || categories.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        categories.forEach(category -> stringBuilder.append(category.getName()).append(", "));

        return stringBuilder.substring(0, stringBuilder.length() - 2);
    }
}
