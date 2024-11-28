package pl.mateuszmarcyk.charity_donation_app.userprofile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;

@RequiredArgsConstructor
@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserService userService;


    public UserProfile findById(Long id) {
        return userProfileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Brak profilu", "Nie znaleziono takiego profilu"));
    }


}
