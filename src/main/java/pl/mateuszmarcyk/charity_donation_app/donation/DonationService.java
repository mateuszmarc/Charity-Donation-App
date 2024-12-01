package pl.mateuszmarcyk.charity_donation_app.donation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.event.DonationProcessCompleteEvent;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DonationService {

    private final DonationRepository donationRepository;
    private final ApplicationEventPublisher publisher;

    public Integer countAllDonations() {
        Integer allDonations = donationRepository.countAll();
        return allDonations == null ? 0 : allDonations;
    }

    public Integer countAllBags() {
        Integer allBags =  donationRepository.countAllBags();
        return allBags == null ? 0 : allBags;
    }

    public void save(@Valid Donation donation) {
        Donation savedDonation = donationRepository.save(donation);

        publisher.publishEvent(new DonationProcessCompleteEvent(savedDonation, donation.getUser()));
    }

    public List<Donation> findAllDonationsForUserSortedByCreated(Long id) {
        return donationRepository.findAllSortedByCreation(id);
    }

    public Donation getDonationById(Long id) {
       return donationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Dar nie istnieje", "Ten dar nie istnieje"));
    }

    public void archiveDonation(Donation donationToArchive) {
        donationToArchive.setReceived(true);
        donationToArchive.setDonationPassedTime(LocalDateTime.now());
        donationRepository.save(donationToArchive);
    }
}
