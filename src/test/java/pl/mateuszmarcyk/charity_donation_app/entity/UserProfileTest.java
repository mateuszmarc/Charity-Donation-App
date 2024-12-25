package pl.mateuszmarcyk.charity_donation_app.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UserProfileTest {

    @Test
    void givenUserProfileAndNoPhotoAndNoId_whenGetPhotosImagePath_thenPathIsNull() {
        UserProfile userProfile = new UserProfile();

        String profileImagePath = userProfile.getPhotosImagePath();

        assertNull(profileImagePath);
    }

    @Test
    void givenUserProfileWithIdAndNoPhoto_whenGetPhotosImagePath_thenPathIsNull() {
        UserProfile userProfile = new UserProfile();
        userProfile.setId(1L);

        String profileImagePath = userProfile.getPhotosImagePath();

        assertNull(profileImagePath);
    }

    @Test
    void givenUserProfileWithNoId_whenGetPhotosImagePath_thenPathIsNull() {
        UserProfile userProfile = new UserProfile();
        userProfile.setProfilePhoto("profile.jpeg");

        String profileImagePath = userProfile.getPhotosImagePath();

        assertNull(profileImagePath);
    }

    @Test
    void givenUserProfile_whenGetPhotosImagePath_thenPathIsGenerated() {
        UserProfile userProfile = new UserProfile();
        userProfile.setId(1L);
        userProfile.setProfilePhoto("profile.jpeg");

        String expectedProfileImagePath = "/photos/users/" + userProfile.getId() + "/" + userProfile.getProfilePhoto();
        String profileImagePath = userProfile.getPhotosImagePath();

        assertThat(profileImagePath).isEqualTo(expectedProfileImagePath);
    }
}