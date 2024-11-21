package pl.mateuszmarcyk.charity_donation_app.usertype;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;

@RequiredArgsConstructor
@Service
public class UserTypeService {

    @Value("${error.resourcenotfound.title}")
    private String resourceNotFoundTitle;
    @Value("${error.resourcenotfound.message}")
    private String userTypeNotFoundMessage;

    private final UserTypeRepository userTypeRepository;

    public UserType findById(Long userTypeId) {
        return userTypeRepository.findById(userTypeId).orElseThrow(() -> new ResourceNotFoundException(resourceNotFoundTitle, userTypeNotFoundMessage));
    }
}
