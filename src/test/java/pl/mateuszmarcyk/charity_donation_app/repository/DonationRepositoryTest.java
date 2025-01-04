package pl.mateuszmarcyk.charity_donation_app.repository;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.mateuszmarcyk.charity_donation_app.entity.Category;
import pl.mateuszmarcyk.charity_donation_app.entity.Donation;
import pl.mateuszmarcyk.charity_donation_app.entity.Institution;
import pl.mateuszmarcyk.charity_donation_app.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Transactional
@Sql(scripts = "classpath:setup-data.sql")
@DataJpaTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class DonationRepositoryTest {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    TestEntityManager testEntityManager;

    @Test
    void givenDonationRepository_whenCountDonations_thenReturnZero() {

        Integer donations = donationRepository.countAll();

        assertThat(donations).isEqualTo(0);
    }

    @Test
    void givenDonationRepository_whenCountDonations_thenReturnOne() {

        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donation = getDonation(user, institution, category);

        testEntityManager.persist(donation);


        Integer donations = donationRepository.countAll();

        assertThat(donations).isEqualTo(1);
    }

    private static Donation getDonation(User user, Institution institution, Category category) {
        return new Donation(
                LocalDateTime.parse("2024-12-24T12:00:00"),
                false,
                user,
                institution,
                new ArrayList<>(List.of(category)),
                "123456789",
                "Please call on arrival.",
                LocalTime.parse("10:30:00"),
                LocalDate.now().plusDays(5),
                "12-345",
                "Kindness City",
                "123 Charity Lane",
                5
        );
    }

    @Test
    void givenDonationRepository_whenCountDonations_thenReturnTwo() {
        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donationOne = getDonation(user, institution, category);


        Donation donationTwo = getDonation(user, institution, category);


        testEntityManager.persist(donationOne);
        testEntityManager.persist(donationTwo);

        Integer donations = donationRepository.countAll();

        assertThat(donations).isEqualTo(2);
    }

    @Test
    void givenDonationRepository_whenCountAllBags_thenReturnFive() {
        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donation = getDonation(user, institution, category);

        testEntityManager.persist(donation);
        Integer bagsQuantity = donationRepository.countAllBags();

        assertThat(bagsQuantity).isEqualTo(5);
    }

    @Test
    void givenDonationRepositoryAndZeroDonations_whenCountAllBags_thenReturnZero() {
        Integer bagsQuantity = donationRepository.countAllBags();

        assertThat(bagsQuantity).isNull();
    }

    @Test
    void givenDonationRepository_whenCountAllBags_thenReturnTen() {
        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donationOne = getDonation(user, institution, category);

        Donation donationTwo = getDonation(user, institution, category);

        testEntityManager.persist(donationOne);
        testEntityManager.persist(donationTwo);

        Integer bagsQuantity = donationRepository.countAllBags();

        assertThat(bagsQuantity).isEqualTo(10);
    }

    @Test
    void givenDonationRepositoryAndNoDonations_whenFindAllDonationsByUserIdSortedByCreation_thenListIsEmpty() {

        List<Donation> donations = donationRepository.findAllDonationsByUserIdSortedByCreation(2L);

        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllDonationsByNullUserIdSortedByCreation_thenListIsEmpty() {

        List<Donation> donations = donationRepository.findAllDonationsByUserIdSortedByCreation(null);

        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepositoryAndNoDUserInDatabase_whenFindAllDonationsByUserIdSortedByCreation_thenListIsEmpty() {
        List<Donation> donations = donationRepository.findAllDonationsByUserIdSortedByCreation(4L);

        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllSortedByCreation_thenListIsEmpty() {
        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donationOne = getDonation(user, institution, category);

        Donation donationTwo = getDonation(user, institution, category);
        testEntityManager.persist(donationOne);
        testEntityManager.persist(donationTwo);

        List<Donation> donations = donationRepository.findAllDonationsByUserIdSortedByCreation(null);

        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllSortedByCreation_thenListIsDonationsByUserIdSortedByCreationTime() {
        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donationOne = getDonation(user, institution, category);

        Donation donationTwo = getDonation(user, institution, category);

        testEntityManager.persist(donationOne);
        testEntityManager.persist(donationTwo);

        List<Donation> donations = donationRepository.findAllDonationsByUserIdSortedByCreation(user.getId());

        assertAll(
                () -> assertThat(donations).isSortedAccordingTo(Comparator.comparing(Donation::getCreated)),
                () -> assertThat(donations).hasSize(2));
    }

    @Test
    void givenDonationRepository_whenFindAllDonationsByUserInDatabaseAndNoDonations_thenListIsEmpty() {
        User user = testEntityManager.find(User.class, 2L);

        List<Donation> donations = donationRepository.findAllDonationsByUser(user);
        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllDonationsByNullUserInDatabaseAndNoDonations_thenListIsEmpty() {
        List<Donation> donations = donationRepository.findAllDonationsByUser(null);
        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllDonationsByUser_thenListSizeIsTwo() {
        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donationOne = getDonation(user, institution, category);

        Donation donationTwo = getDonation(user, institution, category);

        testEntityManager.persist(donationOne);
        testEntityManager.persist(donationTwo);

        List<Donation> donations = donationRepository.findAllDonationsByUser(user);
        assertThat(donations).hasSize(2);
    }

    @Test
    void givenDonationRepository_whenFindAllDonationsByUserSortedByCreated_thenReturnEmptyList() {
        User user = testEntityManager.find(User.class, 2L);

        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByCreated(user);

        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllDonationsByNullUserSortedByCreated_thenReturnEmptyList() {
        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByCreated(null);

        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllDonationsByUserThatDoesNotExistSortedByCreated_thenReturnEmptyList() {
        User user = testEntityManager.find(User.class, 4L);

        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByCreated(user);

        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllDonationsByUserSortedByCreated_thenListSorted() {

        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donationOne = getDonation(user, institution, category);

        Donation donationTwo = getDonation(user, institution, category);

        testEntityManager.persist(donationOne);
        testEntityManager.persist(donationTwo);

        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByCreated(user);

        assertAll(
                () -> assertThat(donations).isSortedAccordingTo(Comparator.comparing(Donation::getCreated).reversed()),
                () -> assertThat(donations).hasSize(2));
    }


    @Test
    void givenDonationRepository_whenFindAllDonationsByUserSortedByQuantityDesc_thenListIsEmpty() {
        User user = testEntityManager.find(User.class, 2L);

        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByQuantityDesc(user);

        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllDonationByNullUserSortedByQuantityDesc_thenListIsEmpty() {
        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByQuantityDesc(null);

        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllDonationByUserNotPresentSortedByQuantityDesc_thenListIsEmpty() {
        User user = testEntityManager.find(User.class, 4L);

        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByQuantityDesc(user);

        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllDonationsByUserSortedByQuantityDesc_thenListIsSorted() {
        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donationOne = getDonation(user, institution, category);
        donationOne.setQuantity(10);

        Donation donationTwo = getDonation(user, institution, category);

        testEntityManager.persist(donationOne);
        testEntityManager.persist(donationTwo);

        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByQuantityDesc(user);

        System.out.println(donations);
        assertAll(
                () -> assertThat(donations).hasSize(2),
                () -> assertThat(donations).isSortedAccordingTo(Comparator.comparing(Donation::getQuantity).reversed())
        );
    }

    @Test
    void givenDonationRepository_whenFindAllDonationsByUserSortedByQuantityAsc_thenListIsEmpty() {
        User user = testEntityManager.find(User.class, 2L);

        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByQuantityAsc(user);

        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllDonationByNullUserSortedByQuantityAsc_thenListIsEmpty() {
        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByQuantityAsc(null);

        assertThat(donations).isEmpty();
    }


    @Test
    void givenDonationRepository_whenFindAllDonationByUserNotPresentSortedByQuantityAsc_thenListIsEmpty() {
        User user = testEntityManager.find(User.class, 4L);

        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByQuantityAsc(user);

        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllDonationsByUserSortedByQuantityAsc_thenListIsSorted() {
        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donationOne = getDonation(user, institution, category);
        donationOne.setQuantity(10);

        Donation donationTwo = getDonation(user, institution, category);

        testEntityManager.persist(donationOne);
        testEntityManager.persist(donationTwo);

        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByQuantityAsc(user);

        System.out.println(donations);
        assertAll(
                () -> assertThat(donations).hasSize(2),
                () -> assertThat(donations).isSortedAccordingTo(Comparator.comparing(Donation::getQuantity))
        );
    }

    @Test
    void givenDonationRepository_whenFindAllDonationsByUserSortedByReceived_thenListIsEmpty() {
        User user = testEntityManager.find(User.class, 2L);

        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByReceived(user);

        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllDonationByNullUserSortedByReceived_thenListIsEmpty() {
        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByReceived(null);

        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllDonationByUserNotPresentSortedByReceived_thenListIsEmpty() {
        User user = testEntityManager.find(User.class, 4L);

        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByReceived(user);

        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllDonationsByUserSortedByReceived_thenListIsSorted() {
        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donationOne = getDonation(user, institution, category);
        donationOne.setQuantity(10);

        Donation donationTwo = getDonation(user, institution, category);

        testEntityManager.persist(donationOne);
        testEntityManager.persist(donationTwo);

        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByReceived(user);

        System.out.println(donations);
        assertAll(
                () -> assertThat(donations).hasSize(2),
                () -> assertThat(donations).isSortedAccordingTo(Comparator.comparing(Donation::isReceived))
        );
    }

    @Test
    void givenDonationRepository_whenFindAllDonationsByUserSortedByUnreceived_thenListIsEmpty() {
        User user = testEntityManager.find(User.class, 2L);

        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByUnreceived(user);

        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllDonationByNullUserSortedByUnreceived_thenListIsEmpty() {
        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByUnreceived(null);

        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllDonationByUserNotPresentSortedByUnreceived_thenListIsEmpty() {
        User user = testEntityManager.find(User.class, 4L);

        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByUnreceived(user);

        assertThat(donations).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindAllDonationsByUserSortedByUnreceived_thenListIsSorted() {
        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donationOne = getDonation(user, institution, category);
        donationOne.setQuantity(10);

        Donation donationTwo = getDonation(user, institution, category);

        testEntityManager.persist(donationOne);
        testEntityManager.persist(donationTwo);

        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByUnreceived(user);

        System.out.println(donations);
        assertAll(
                () -> assertThat(donations).hasSize(2),
                () -> assertThat(donations).isSortedAccordingTo(Comparator.comparing(Donation::isReceived).reversed())
        );
    }
}