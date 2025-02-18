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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Transactional
@Sql(scripts = "classpath:setup-data.sql")
@DataJpaTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    TestEntityManager testEntityManager;


    @Test
    void givenCategoryRepository_whenFindByIdFetchDonations_thenReturnCategoryWithoutDonations() {

        Optional<Category> optionalCategory = categoryRepository.findByIdFetchDonations(2L);

        assertAll(
                () -> assertThat(optionalCategory).isPresent(),
                () -> assertThat(optionalCategory.get().getId()).isEqualTo(2),
                () -> assertThat(optionalCategory.get().getName()).isEqualTo("Zabawki"),
                () -> assertThat(optionalCategory.get().getDonations()).isEmpty()
        );
    }



    @Test
    void givenCategoryRepository_whenFindByIdFetchDonations_thenReturnCategoryWithDonations() {

        Institution institution = testEntityManager.find(Institution.class, 1L);
        User user = testEntityManager.find(User.class, 2L);
        Category category = testEntityManager.find(Category.class, 1L);

        Donation donationOne = TestDataFactory.getDonationForRepositoryTest(user, institution, category);

        Donation donationTwo = TestDataFactory.getDonationForRepositoryTest(user, institution, category);
        testEntityManager.persist(donationOne);
        testEntityManager.persist(donationTwo);

        Optional<Category> optionalCategory = categoryRepository.findByIdFetchDonations(1L);

        assertAll(
                () -> assertThat(optionalCategory).isPresent(),
                () -> assertThat(optionalCategory.get().getId()).isEqualTo(1),
                () -> assertThat(optionalCategory.get().getName()).isEqualTo("Jedzenie"),
                () -> assertThat(optionalCategory.get().getDonations()).hasSize(2)
        );
    }


    @Test
    void givenCategoryRepository_whenFindByIdNotInDatabase_thenReturnEmpty() {

        Optional<Category> optionalCategory = categoryRepository.findByIdFetchDonations(111L);

        assertThat(optionalCategory).isEmpty();
    }

    @Test
    void givenCategoryRepository_whenFindByNullIdNotInDatabase_thenReturnEmpty() {

        Optional<Category> optionalCategory = categoryRepository.findByIdFetchDonations(null);

        assertThat(optionalCategory).isEmpty();
    }
}