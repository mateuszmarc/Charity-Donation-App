package pl.mateuszmarcyk.charity_donation_app.usertype;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;

@RequiredArgsConstructor
@Service
public class UserTypeService {

    private final UserTypeRepository userTypeRepository;

    public UserType findById(Long userTypeId) {
        return userTypeRepository.findById(userTypeId).orElseThrow(() -> new ResourceNotFoundException("Could not find userType record in database"));
    }
}
