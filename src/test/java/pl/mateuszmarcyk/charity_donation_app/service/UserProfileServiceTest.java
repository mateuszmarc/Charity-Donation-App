package pl.mateuszmarcyk.charity_donation_app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.repository.UserProfileRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @InjectMocks
    private UserProfileService userProfileService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Test
    void givenUserProfileService_whenFindById_thenThrowResourceNotFoundException() {
        Long id = null;
        when(userProfileRepository.findById(id)).thenReturn(Optional.empty());
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        assertThatThrownBy(() -> userProfileService.findById(id)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Nie znaleziono takiego profilu");
        verify(userProfileRepository).findById(longArgumentCaptor.capture());
        Long usedId = longArgumentCaptor.getValue();

        assertThat(usedId).isEqualTo(id);
    }

    @Test
    void givenUserProfileService_whenFindById_ThenUserProfileFound() {
        Long id = 1L;
        UserProfile userProfile = new UserProfile();
        userProfile.setId(1L);

        when(userProfileRepository.findById(id)).thenReturn(Optional.of(userProfile));
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        UserProfile foundUserProfile = userProfileService.findById(id);
        verify(userProfileRepository).findById(longArgumentCaptor.capture());
        Long usedId = longArgumentCaptor.getValue();

        assertThat(foundUserProfile).isEqualTo(userProfile);
        assertThat(usedId).isEqualTo(id);
    }
}