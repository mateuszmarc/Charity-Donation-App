package pl.mateuszmarcyk.charity_donation_app.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggedUserModelHandlerTest {

    @InjectMocks
    private LoggedUserModelHandler loggedUserModelHandler;

    @Mock
    private CustomUserDetails userDetails;

    @Test
    void givenCustomUserDetails_thenGetUser_thenUserReturned() {
        User user = new User();
        user.setId(1L);

        when(userDetails.getUser()).thenReturn(user);

        User returned = loggedUserModelHandler.getUser(userDetails);
        assertThat(returned).isEqualTo(user);
        verify(userDetails, times(1)).getUser();
    }

    @Test
    void givenUser_thenAddUserToModel_thenAttributesAreAdded() {
        User user = new User();
        UserProfile profile = new UserProfile();
        user.setProfile(profile);
        Model model = new ConcurrentModel();

        loggedUserModelHandler.addUserToModel(user, model);

        assertAll(
                () -> assertThat(model.containsAttribute("user")).isTrue(),
                () -> assertThat(model.containsAttribute("userProfile")).isTrue(),
                () -> assertThat(model.getAttribute("user")).isEqualTo(user),
                () -> assertThat(model.getAttribute("userProfile")).isEqualTo(profile)
        );
    }

}

