package pl.mateuszmarcyk.charity_donation_app.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.mateuszmarcyk.charity_donation_app.entity.*;
import pl.mateuszmarcyk.charity_donation_app.validation.MailMessageTestData;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MailMessageTest {

    private MailMessage mailMessage = new MailMessage();

    private static Stream<Arguments> provideBuildDonationMessagesForVariousUserFirstName() {
        return Stream.of(
                Arguments.of(null, MailMessageTestData.DONATION_WITH_USER_WITH_NULL_FIRST_NAME_EXPECTED_MESSAGE),
                Arguments.of("", MailMessageTestData.DONATION_WITH_USER_FIRST_NAME_EMPTY_EXPECTED_MESSAGE),
                Arguments.of("Mateusz", MailMessageTestData.DONATION_WITH_USER_FIRST_NAME_NOT_EMPTY_EXPECTED_MESSAGE)
        );
    }

    private static Stream<Arguments> provideBuildDonationMessagesForVariousDonationComment() {
        return Stream.of(
                Arguments.of(null, MailMessageTestData.DONATION_WITH_NULL_DONATION_COMMENT_EXPECTED_MESSAGE),
                Arguments.of("", MailMessageTestData.DONATION_WITH_EMPTY_DONATION_COMMENT_EXPECTED_MESSAGE),
                Arguments.of("Proszę zadzwonić", MailMessageTestData.DONATION_WITH_DONATION_COMMENT_EXPECTED_MESSAGE)
        );
    }

    @Test
    void whenBuildMessage_thenMessageMatches() {
        String url = "http://localhost/app/example";
        String builtMessage = mailMessage.buildMessage(url);
        assertThat(builtMessage).isEqualTo(MailMessageTestData.BUILD_MAIL_MESSAGE_EXPECTED_MESSAGE);
    }

    @ParameterizedTest
    @MethodSource("provideBuildDonationMessagesForVariousUserFirstName")
    void givenDonationWithUserVariousFirstName_whenBuildDonationMessage_thenMessageMatches(String firstName, String expectedMessage) {
        Donation donation = spy(getDonation());
        donation.getUser().getProfile().setFirstName(firstName);

        String builtMessage = mailMessage.buildDonationMessage(donation);

        verify(donation, times(2)).getUser();
        verify(donation, times(1)).getInstitution();
        verify(donation, times(1)).getQuantity();
        verify(donation, times(1)).getCategoriesString();
        verify(donation, times(1)).getStreet();
        verify(donation, times(1)).getCity();
        verify(donation, times(1)).getZipCode();
        verify(donation, times(1)).getPickUpDate();
        verify(donation, times(1)).getPickUpTime();
        verify(donation, times(1)).getPhoneNumber();

        assertThat(builtMessage).isEqualTo(expectedMessage);
    }


    @ParameterizedTest
    @MethodSource("provideBuildDonationMessagesForVariousDonationComment")
    void givenDonationWithVariousDonationComment_whenBuildDonationMessage_thenMessageMatches(String donationComment, String expectedMessage) {
        Donation donation = spy(getDonation());
        donation.setPickUpComment(donationComment);

        String builtMessage = mailMessage.buildDonationMessage(donation);

        verify(donation, times(1)).getUser();
        verify(donation, times(1)).getInstitution();
        verify(donation, times(1)).getQuantity();
        verify(donation, times(1)).getCategoriesString();
        verify(donation, times(1)).getStreet();
        verify(donation, times(1)).getCity();
        verify(donation, times(1)).getZipCode();
        verify(donation, times(1)).getPickUpDate();
        verify(donation, times(1)).getPickUpTime();
        verify(donation, times(1)).getPhoneNumber();
        assertThat(builtMessage).isEqualTo(expectedMessage);
    }

    @Test
    void whenGetMailMessage_thenMessageMatches() {
        MessageDTO messageDTO = new MessageDTO( "Mateusz",  "Marcykiewicz", "Random message", "test@gmail.com");


        String builtMessage = mailMessage.getMailMessage(messageDTO);
        assertThat(builtMessage).isEqualTo(MailMessageTestData.GET_MAIL_MESSAGE_EXPECTED_DATA);
    }

    @Test
    void whenBuildPasswordResetMessage_thenMessageMatches() {
        String url = "http://localhost/app/example";


        String builtMessage = mailMessage.buildPasswordResetMessage(url);
        assertThat(builtMessage).isEqualTo(MailMessageTestData.BUILD_PASSWORD_RESET_MESSAGE_EXPECTED_MESSAGE);
    }

    private static Donation getDonation() {
        UserProfile userProfile = new UserProfile();
        userProfile.setFirstName("Mateusz");

        User user = new User();
        user.setUserProfile(userProfile);


        Institution institution = new Institution(2L, "Pomagam", "Description", new ArrayList<>());

        Category firstCategory = new Category(1L, "Ubranie", new ArrayList<>());
        Category secondCategory = new Category(2L, "Jedzenie", new ArrayList<>());

        List<Category> categories = List.of(firstCategory, secondCategory);

        return new Donation(
                null,
                false,
                user,
                institution,
                categories,
                "123456789",
                "Pickup Comment",
                LocalTime.of(10, 0),
                LocalDate.of(2024, 12, 31),
                "12-345",
                "Sample City",
                "Sample Street",
                5
        );
    }
}