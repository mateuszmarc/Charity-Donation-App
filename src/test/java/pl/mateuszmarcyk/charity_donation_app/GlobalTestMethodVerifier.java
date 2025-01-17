package pl.mateuszmarcyk.charity_donation_app;

import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.entity.Donation;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.util.LoggedUserModelHandler;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
            assertThat(modelAndView.getModel()).containsEntry(key, value);
        });
    }

    public static void assertEmptyDonation(Donation donation) {
        assertAll(
                () -> assertThat(donation.getId()).isNull(),
                () -> assertThat(donation.getQuantity()).isNull(),
                () -> assertThat(donation.getStreet()).isNull(),
                () -> assertThat(donation.getCity()).isNull(),
                () -> assertThat(donation.getZipCode()).isNull(),
                () -> assertThat(donation.getPickUpDate()).isNull(),
                () -> assertThat(donation.getPickUpTime()).isNull(),
                () -> assertThat(donation.getPickUpComment()).isNull(),
                () -> assertThat(donation.getPhoneNumber()).isNull(),
                () -> assertThat(donation.getCategories()).isNull(),
                () -> assertThat(donation.getInstitution()).isNull(),
                () -> assertThat(donation.getUser()).isNull(),
                () -> assertThat(donation.getCreated()).isNull(),
                () -> assertThat(donation.isReceived()).isFalse(),
                () -> assertThat(donation.getDonationPassedTime()).isNull()
        );
    }

    public static void verifyMessageSourceInteraction(MessageSource messageSource, String message) {
        verify(messageSource, times(1)).getMessage(message, null, Locale.getDefault());

    }
}
