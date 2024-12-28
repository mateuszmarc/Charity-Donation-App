package pl.mateuszmarcyk.charity_donation_app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.repository.UserProfileRepository;

@RequiredArgsConstructor
@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfile findById(Long id) {
        return userProfileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Brak profilu", "Nie znaleziono takiego profilu"));
    }


}
