package pl.mateuszmarcyk.charity_donation_app.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.SpringConstraintValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ValidatorConfigTest {

    @InjectMocks
    private ValidatorConfig validatorConfig;

    @Mock
    private SpringConstraintValidatorFactory factoryBean;

    @Test
    void givenConfig_whenValidatorFactoryBeanCreated_thenConfiguredCorrectly() {
        // Act
        LocalValidatorFactoryBean validatorFactoryBean = validatorConfig.validatorFactoryBean();

        validatorFactoryBean.afterPropertiesSet();

        // Assert
        assertThat(validatorFactoryBean).isNotNull();
        assertThat(validatorFactoryBean.getConstraintValidatorFactory()).isEqualTo(factoryBean);
    }
}
