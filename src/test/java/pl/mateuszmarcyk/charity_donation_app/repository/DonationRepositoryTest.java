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
import pl.mateuszmarcyk.charity_donation_app.TestDataFactory;
import pl.mateuszmarcyk.charity_donation_app.entity.Category;
import pl.mateuszmarcyk.charity_donation_app.entity.Donation;
import pl.mateuszmarcyk.charity_donation_app.entity.Institution;
import pl.mateuszmarcyk.charity_donation_app.entity.User;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

        assertThat(donations).isZero();
    }

    @Test
    void givenDonationRepository_whenCountDonations_thenReturnOne() {

        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donation = TestDataFactory.getDonationForRepositoryTest(user, institution, category);

        testEntityManager.persist(donation);


        Integer donations = donationRepository.countAll();

        assertThat(donations).isEqualTo(1);
    }

    @Test
    void givenDonationRepository_whenCountDonations_thenReturnTwo() {
        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donationOne = TestDataFactory.getDonationForRepositoryTest(user, institution, category);


        Donation donationTwo = TestDataFactory.getDonationForRepositoryTest(user, institution, category);


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

        Donation donation = TestDataFactory.getDonationForRepositoryTest(user, institution, category);

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

        Donation donationOne = TestDataFactory.getDonationForRepositoryTest(user, institution, category);

        Donation donationTwo = TestDataFactory.getDonationForRepositoryTest(user, institution, category);

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

        Donation donationOne = TestDataFactory.getDonationForRepositoryTest(user, institution, category);

        Donation donationTwo = TestDataFactory.getDonationForRepositoryTest(user, institution, category);
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

        Donation donationOne = TestDataFactory.getDonationForRepositoryTest(user, institution, category);

        Donation donationTwo = TestDataFactory.getDonationForRepositoryTest(user, institution, category);

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

        Donation donationOne = TestDataFactory.getDonationForRepositoryTest(user, institution, category);

        Donation donationTwo = TestDataFactory.getDonationForRepositoryTest(user, institution, category);

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

        Donation donationOne = TestDataFactory.getDonationForRepositoryTest(user, institution, category);

        Donation donationTwo = TestDataFactory.getDonationForRepositoryTest(user, institution, category);

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

        Donation donationOne = TestDataFactory.getDonationForRepositoryTest(user, institution, category);
        donationOne.setQuantity(10);

        Donation donationTwo = TestDataFactory.getDonationForRepositoryTest(user, institution, category);

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

        Donation donationOne = TestDataFactory.getDonationForRepositoryTest(user, institution, category);
        donationOne.setQuantity(10);

        Donation donationTwo = TestDataFactory.getDonationForRepositoryTest(user, institution, category);

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

        Donation donationOne = TestDataFactory.getDonationForRepositoryTest(user, institution, category);
        donationOne.setQuantity(10);

        Donation donationTwo = TestDataFactory.getDonationForRepositoryTest(user, institution, category);

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

        Donation donationOne = TestDataFactory.getDonationForRepositoryTest(user, institution, category);
        donationOne.setQuantity(10);

        Donation donationTwo = TestDataFactory.getDonationForRepositoryTest(user, institution, category);

        testEntityManager.persist(donationOne);
        testEntityManager.persist(donationTwo);

        List<Donation> donations = donationRepository.findAllDonationsByUserSortedByUnreceived(user);

        System.out.println(donations);
        assertAll(
                () -> assertThat(donations).hasSize(2),
                () -> assertThat(donations).isSortedAccordingTo(Comparator.comparing(Donation::isReceived).reversed())
        );
    }

    @Test
    void givenDonationRepository_whenFindUserDonationById_thenDonationFound() {
        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donationOne = TestDataFactory.getDonationForRepositoryTest(user, institution, category);
        donationOne.setQuantity(10);

        Donation savedDonationOne = testEntityManager.persist(donationOne);

        Optional<Donation> optionalDonation = donationRepository.findUserDonationById(user, savedDonationOne.getId());

        assertAll(
                () -> assertThat(optionalDonation).isPresent(),
                () -> assertThat(optionalDonation).contains(savedDonationOne),
                () -> assertThat(optionalDonation.get().getUser()).isEqualTo(user)
        );
    }

    @Test
    void givenDonationRepository_whenFindUserDonationByIdForNullUser_thenDonationNotFound() {
        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donationOne = TestDataFactory.getDonationForRepositoryTest(user, institution, category);
        donationOne.setQuantity(10);

        Donation savedDonationOne = testEntityManager.persist(donationOne);

        Optional<Donation> optionalDonation = donationRepository.findUserDonationById(null, savedDonationOne.getId());

        assertThat(optionalDonation).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindUserDonationByIdForNullUserAndNullDonationId_thenDonationNotFound() {
        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donationOne = TestDataFactory.getDonationForRepositoryTest(user, institution, category);
        donationOne.setQuantity(10);

        testEntityManager.persist(donationOne);

        Optional<Donation> optionalDonation = donationRepository.findUserDonationById(null, null);

        assertThat(optionalDonation).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindUserDonationByIdForUserAndNullDonationId_thenDonationNotFound() {
        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donationOne = TestDataFactory.getDonationForRepositoryTest(user, institution, category);
        donationOne.setQuantity(10);

        testEntityManager.persist(donationOne);

        Optional<Donation> optionalDonation = donationRepository.findUserDonationById(user, null);

        assertThat(optionalDonation).isEmpty();
    }

    @Test
    void givenDonationRepository_whenFindUserDonationByIdForUserAndDonationNotBelongingToUserId_thenDonationNotFound() {
        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donationOne = TestDataFactory.getDonationForRepositoryTest(user, institution, category);
        donationOne.setQuantity(10);

        Donation donationTwo = TestDataFactory.getDonationForRepositoryTest(null, institution, category);

        testEntityManager.persist(donationOne);
        Donation savedDonationTwo = testEntityManager.persist(donationTwo);

        Optional<Donation> optionalDonation = donationRepository.findUserDonationById(user, savedDonationTwo.getId());

        assertThat(optionalDonation).isEmpty();
    }
}