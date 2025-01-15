package pl.mateuszmarcyk.charity_donation_app.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.SpringConstraintValidatorFactory;

@Configuration
public class ValidatorConfig {

    private final SpringConstraintValidatorFactory constraintValidatorFactory;

    public ValidatorConfig(SpringConstraintValidatorFactory constraintValidatorFactory) {
        this.constraintValidatorFactory = constraintValidatorFactory;
    }

    @Bean
    public LocalValidatorFactoryBean validatorFactoryBean(MessageSource messageSource) {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        factoryBean.setValidationMessageSource(messageSource);
        factoryBean.setConstraintValidatorFactory(constraintValidatorFactory);
        return factoryBean;
    }
}
