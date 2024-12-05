package pl.mateuszmarcyk.charity_donation_app.donation;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.event.DonationProcessCompleteEvent;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.user.User;

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

    @Transactional
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

    @Transactional
    public void archiveDonation(Donation donationToArchive) {
        donationToArchive.setReceived(true);
        donationToArchive.setDonationPassedTime(LocalDateTime.now());
        donationRepository.save(donationToArchive);
    }

    public List<Donation> getDonationsForUserSortedBy(String sortType, User loggedUser) {
        if (sortType != null) {
            return switch (sortType) {
                case "created" -> donationRepository.findAllDonationsByUserSortedByCreated(loggedUser);
                case "quantity desc" -> donationRepository.findAllDonationByUserSortedByQuantityDesc(loggedUser);
                case "quantity asc" -> donationRepository.findAllDonationByUserSortedByQuantityAsc(loggedUser);
                case "received asc" -> donationRepository.findAllDonationByUserSortedByReceived(loggedUser);
                default -> donationRepository.findAllDonationByUserSortedByUnreceived(loggedUser);
            };
        }
            return donationRepository.findAllDonationsByUser(loggedUser);

        }
}
