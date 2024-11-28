package pl.mateuszmarcyk.charity_donation_app.userprofile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;

@RequiredArgsConstructor
@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;


    public UserProfile findById(Long id) {
        return userProfileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Brak profilu", "Nie znaleziono takiego profilu"));
    }
}
