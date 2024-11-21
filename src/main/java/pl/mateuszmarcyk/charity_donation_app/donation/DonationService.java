package pl.mateuszmarcyk.charity_donation_app.donation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DonationService {

    private final DonationRepository donationRepository;

    public long countAllDonations() {
        return donationRepository.countAll();
    }

    public Long countAllBags() {
        return donationRepository.countAllBags();
    }
}
