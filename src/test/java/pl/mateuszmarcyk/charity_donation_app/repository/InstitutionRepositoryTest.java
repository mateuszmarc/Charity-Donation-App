package pl.mateuszmarcyk.charity_donation_app.repository;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.mateuszmarcyk.charity_donation_app.entity.Institution;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class InstitutionRepositoryTest {

    @Autowired
    private InstitutionRepository institutionRepository;



    @Test
    @Sql(scripts = "classpath:setup-data.sql")
    void givenInstitutionRepository_whenFindAll_thenReturnListOfInstitutions() {
        List<Institution> returnedInstitutions = institutionRepository.findAll();

        assertThat(returnedInstitutions).hasSize(7);
        }
}