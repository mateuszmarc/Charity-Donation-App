package pl.mateuszmarcyk.charity_donation_app.repository;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.mateuszmarcyk.charity_donation_app.entity.Category;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Transactional
@Sql(scripts = "classpath:setup-data.sql")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;



    @Test
    void givenCategoryRepository_whenFindByIdFetchDonations_thenReturnCategoryWithoutDonations() {

        Optional<Category> optionalCategory = categoryRepository.findByIdFetchDonations(2L);

        assertAll(
                () -> assertThat(optionalCategory).isPresent(),
                () -> assertThat(optionalCategory.get().getId()).isEqualTo(2),
                () -> assertThat(optionalCategory.get().getName()).isEqualTo("Zabawki"),
                () -> assertThat(optionalCategory.get().getDonations()).hasSize(0)
        );
    }

    @Test
    void givenCategoryRepository_whenFindByIdFetchDonations_thenReturnCategoryWithDonations() {
        Optional<Category> optionalCategory = categoryRepository.findByIdFetchDonations(1L);

        assertAll(
                () -> assertThat(optionalCategory).isPresent(),
                () -> assertThat(optionalCategory.get().getId()).isEqualTo(1),
                () -> assertThat(optionalCategory.get().getName()).isEqualTo("Jedzenie"),
                () -> assertThat(optionalCategory.get().getDonations()).hasSize(1)
        );
    }

    @Test
    void givenCategoryRepository_whenFindByIdNotInDatabase_thenReturnEmpty() {

        Optional<Category> optionalCategory = categoryRepository.findByIdFetchDonations(111L);

        assertThat(optionalCategory).isEmpty();
    }
}