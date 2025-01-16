package pl.mateuszmarcyk.charity_donation_app.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import pl.mateuszmarcyk.charity_donation_app.TestDataFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class UserTest {

    @Test
    void givenUser_whenAddUserTypeWhichUserAlreadyHave_thenUserTypeNotAdded() {
        UserType userUserType = new UserType(1L, "ROLE_USER", new ArrayList<>());
        UserType adminUserType = new UserType(2L, "ROLE_ADMIN", new ArrayList<>());

        User user = new User();
        user.setUserTypes(new HashSet<>(Set.of(userUserType, adminUserType)));

        user.addUserType(adminUserType);
        user.addUserType(userUserType);

        assertAll(
                () -> assertThat(user.getUserTypes()).hasSize(2),
                () -> assertThat(user.getUserTypes()).contains(userUserType),
                () -> assertThat(user.getUserTypes()).contains(adminUserType)
        );
    }

    @Test
    void givenUser_whenAddAdminUserType_thenUserTypeIsAdded() {
        UserType adminUserType = new UserType(2L, "ROLE_ADMIN", new ArrayList<>());

        User user = new User();
        user.setUserTypes(new HashSet<>(Set.of(adminUserType)));

        user.addUserType(adminUserType);

        assertAll(
                () -> assertThat(user.getUserTypes()).hasSize(1),
                () -> assertThat(user.getUserTypes()).contains(adminUserType)
        );
    }

    @Test
    void givenUser_whenNullAdminUserType_thenUserTypeIsNotAdded() {
        UserType nullUserType = null;

        User user = new User();
        user.setUserTypes(new HashSet<>());


        user.addUserType(nullUserType);

        assertAll(
                () -> assertThat(user.getUserTypes()).hasSize(0),
                () -> assertThat(user.getUserTypes()).doesNotContain(nullUserType)
        );
    }

    @Test
    void givenUser_whenAddUserType_thenUserTypeIsAdded() {
        UserType userUserType = new UserType(1L, "ROLE_USER", new ArrayList<>());

        User user = new User();
        user.setUserTypes(new HashSet<>(Set.of(userUserType)));

        user.addUserType(userUserType);

        assertAll(
                () -> assertThat(user.getUserTypes()).hasSize(1),
                () -> assertThat(user.getUserTypes()).contains(userUserType)
        );
    }

    @Test
    void givenUser_whenRemoveAdminUserType_ThenUserTypeIsRemoved() {
        UserType userUserType = new UserType(1L, "ROLE_USER", new ArrayList<>());
        UserType adminUserType = new UserType(2L, "ROLE_ADMIN", new ArrayList<>());

        User user = new User();
        user.setUserTypes(new HashSet<>(Set.of(userUserType, adminUserType)));

        user.removeUserType(adminUserType);

        assertAll(
                () -> assertThat(user.getUserTypes()).hasSize(1),
                () -> assertThat(user.getUserTypes()).contains(userUserType),
                () -> assertThat(user.getUserTypes()).doesNotContain(adminUserType)
        );
    }

    @Test
    void givenUser_whenRemoveUserUserType_ThenUserTypeIsRemoved() {
        UserType userUserType = new UserType(1L, "ROLE_USER", new ArrayList<>());
        UserType adminUserType = new UserType(2L, "ROLE_ADMIN", new ArrayList<>());

        User user = new User();
        user.setUserTypes(new HashSet<>(Set.of(userUserType, adminUserType)));

        user.removeUserType(userUserType);

        assertAll(
                () -> assertThat(user.getUserTypes()).hasSize(1),
                () -> assertThat(user.getUserTypes()).contains(adminUserType),
                () -> assertThat(user.getUserTypes()).doesNotContain(userUserType)
        );
    }

    @Test
    void givenUserWithNoUserTypes_whenRemoveUserUserType_ThenUserTypeIsNoRemoved() {
        UserType userUserType = new UserType(1L, "ROLE_USER", new ArrayList<>());
        UserType adminUserType = new UserType(2L, "ROLE_ADMIN", new ArrayList<>());

        User user = new User();
        user.setUserTypes(new HashSet<>());

        user.removeUserType(userUserType);

        assertAll(
                () -> assertThat(user.getUserTypes()).hasSize(0),
                () -> assertThat(user.getUserTypes()).doesNotContain(adminUserType),
                () -> assertThat(user.getUserTypes()).doesNotContain(userUserType)
        );
    }

    @ParameterizedTest(name = "dateTimeString={1}, expected={2}")
    @CsvFileSource(resources = "/donationparameters/datetime-data.csv")
    void givenDonation_whenGetDonationPassedDateTime_thenStringIsFormatted(String dateTimeString, String expected) {
        User user= TestDataFactory.getUser();

        LocalDateTime testDateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
        user.setRegistrationDate(testDateTime);

        String transformedTime = user.getRegistrationDateTime();

        assertThat(transformedTime).isEqualTo(expected);
    }
}