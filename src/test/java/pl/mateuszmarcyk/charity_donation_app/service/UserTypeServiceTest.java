package pl.mateuszmarcyk.charity_donation_app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import pl.mateuszmarcyk.charity_donation_app.entity.UserType;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.repository.UserTypeRepository;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTypeServiceTest {

    @InjectMocks
    private UserTypeService userTypeService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private UserTypeRepository userTypeRepository;

    @Test
    void givenUserTypeService_whenFindById_thenResourceNotFoundExceptionThrown() {
        Long id = 1L;
        String tokenNotFoundTitle = "Test title";
        String tokenNotFoundMessage = "Test message";

        when(messageSource.getMessage("error.resourcenotfound.title", null, Locale.getDefault())).thenReturn(tokenNotFoundTitle);
        when(messageSource.getMessage("error.resourcenotfound.message", null, Locale.getDefault())).thenReturn(tokenNotFoundMessage);

        ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        when(userTypeRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userTypeService.findById(id)).isInstanceOf(ResourceNotFoundException.class).hasMessage(tokenNotFoundMessage);
        verify(userTypeRepository).findById(idArgumentCaptor.capture());
        verify(messageSource, times(1)).getMessage("error.resourcenotfound.title", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("error.resourcenotfound.message", null, Locale.getDefault());
        Long usedId = idArgumentCaptor.getValue();

        assertThat(usedId).isEqualTo(id);
    }

    @Test
    void givenUserTypeService_whenFindById_thenUserTypeReturned() {
        UserType userType = new UserType(1L, "ROLE_ADMIN", new ArrayList<>());

        String tokenNotFoundTitle = "Test title";
        String tokenNotFoundMessage = "Test message";
        ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        when(messageSource.getMessage("error.resourcenotfound.title", null, Locale.getDefault())).thenReturn(tokenNotFoundTitle);
        when(messageSource.getMessage("error.resourcenotfound.message", null, Locale.getDefault())).thenReturn(tokenNotFoundMessage);
        when(userTypeRepository.findById(userType.getId())).thenReturn(Optional.of(userType));

        UserType foundUserType = userTypeService.findById(userType.getId());
        verify(userTypeRepository).findById(idArgumentCaptor.capture());
        verify(messageSource, times(1)).getMessage("error.resourcenotfound.title", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("error.resourcenotfound.message", null, Locale.getDefault());

        Long usedId = idArgumentCaptor.getValue();

        assertAll(
                () -> assertThat(usedId).isEqualTo(userType.getId()),
                () -> assertThat(foundUserType).isEqualTo(userType)
        );
    }
}