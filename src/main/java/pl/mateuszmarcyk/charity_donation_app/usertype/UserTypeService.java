package pl.mateuszmarcyk.charity_donation_app.usertype;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;

import java.util.Locale;

@RequiredArgsConstructor
@Service
public class UserTypeService {

    private final MessageSource messageSource;
    private final UserTypeRepository userTypeRepository;

    public UserType findById(Long userTypeId) {
        String resourceNotFoundTitle = messageSource.getMessage("error.resourcenotfound.title", null, Locale.getDefault());
        String userTypeNotFoundMessage = messageSource.getMessage("error.resourcenotfound.message", null, Locale.getDefault());
        return userTypeRepository.findById(userTypeId).orElseThrow(() -> new ResourceNotFoundException(resourceNotFoundTitle, userTypeNotFoundMessage));
    }
}
