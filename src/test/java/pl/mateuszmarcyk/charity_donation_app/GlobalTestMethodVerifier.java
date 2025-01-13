package pl.mateuszmarcyk.charity_donation_app;

import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.util.LoggedUserModelHandler;

import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GlobalTestMethodVerifier {

    public static void verifyInvocationOfLoggedUserModelHandlerMethods(LoggedUserModelHandler loggedUserModelHandler) {
        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));
    }

    public static void assertMvcResult(MvcResult mvcResult, String expectedViewName, int expectedStatus) {
        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertAll(
                () -> assertThat(mvcResult.getResponse().getStatus()).isEqualTo(expectedStatus),
                () -> assertThat(modelAndView).isNotNull(),
                () -> assertThat(modelAndView.getViewName()).isEqualTo(expectedViewName)
        );
    }

    public static void assertModelAndViewAttributes(MvcResult mvcResult, Map<String, Object> attributes) {
        ModelAndView modelAndView = mvcResult.getModelAndView();

        attributes.forEach((key, value) -> {
            if (value instanceof Collection<?> collection) {
                assertIterableEquals(collection, (Iterable<?>) modelAndView.getModel().get(key));
            }
            assertThat(modelAndView.getModel().get(key)).isEqualTo(value);
        });
    }
}
