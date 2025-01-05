package pl.mateuszmarcyk.charity_donation_app.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ValidatorConfigIntegrationTest {

    @Autowired
    private LocalValidatorFactoryBean localValidatorFactoryBean;

    @Test
    void givenContext_whenValidatorFactoryBeanRetrieved_thenConfiguredCorrectly() {
        // Assert
        assertThat(localValidatorFactoryBean).isNotNull();
        assertThat(localValidatorFactoryBean.getConstraintValidatorFactory()).isNotNull();
    }
}