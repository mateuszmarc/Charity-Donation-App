package pl.mateuszmarcyk.charity_donation_app.util.constraintannotations;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringConstraintValidatorFactory implements ConstraintValidatorFactory {

    private final ApplicationContext applicationContext;

    @Autowired
    public SpringConstraintValidatorFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
        try {
            return applicationContext.getBean(key);
        } catch (Exception e) {
            try {
                return key.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new RuntimeException("Unable to instantiate validator: " + key, ex);
            }
        }
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {
        // Let Spring handle the bean lifecycle
    }
}
