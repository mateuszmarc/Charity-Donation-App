package pl.mateuszmarcyk.charity_donation_app.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.entity.Donation;
import pl.mateuszmarcyk.charity_donation_app.entity.Institution;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.repository.DonationRepository;
import pl.mateuszmarcyk.charity_donation_app.util.event.DonationProcessCompleteEvent;

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

    public Donation findDonationById(Long id) {
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
                case "quantity desc" -> donationRepository.findAllDonationsByUserSortedByQuantityDesc(loggedUser);
                case "quantity asc" -> donationRepository.findAllDonationsByUserSortedByQuantityAsc(loggedUser);
                case "received asc" -> donationRepository.findAllDonationsByUserSortedByReceived(loggedUser);
                default -> donationRepository.findAllDonationsByUserSortedByUnreceived(loggedUser);
            };
        }
            return donationRepository.findAllDonationsByUser(loggedUser);

        }

    public List<Donation> findAll(String sortType) {
        if (sortType != null) {
            return switch (sortType) {
                case "quantity desc" -> donationRepository.findAllDonationsByQuantityDesc();
                case "quantity asc" -> donationRepository.findAllDonationsSortedByQuantityAsc();
                case "received asc" -> donationRepository.findAllDonationsSortedByReceivedAsc();
                default -> donationRepository.findAllDonationsSortedByCreated();
            };
        }
        return donationRepository.findAllDonationsSortedByCreated();
    }

    @Transactional
    public void unArchiveDonation(Donation donationToArchive) {
        donationToArchive.setReceived(false);
        donationToArchive.setDonationPassedTime(null);
        donationRepository.save(donationToArchive);
    }

    @Transactional
    public void deleteDonation(Donation donationToDelete) {
        donationToDelete.getCategories().forEach(category -> category.getDonations().removeIf(donation -> donation.getId().equals(donationToDelete.getId())));
        Institution institution = donationToDelete.getInstitution();
        if (institution != null) {
            institution.getDonations().removeIf(donation -> donation.getId().equals(donationToDelete.getId()));
        }
        User user = donationToDelete.getUser();
        if (user != null) {
            user.getDonations().removeIf(donation -> donation.getId().equals(donationToDelete.getId()));
        }

        donationRepository.delete(donationToDelete);
    }

    public Donation getUserDonationById(User owner, Long id) {
        return donationRepository.findUserDonationById(owner, id).orElseThrow(() -> new ResourceNotFoundException("Dar nie istnieje", "Ten dar nie istnieje"));
    }

    @Transactional
    public void archiveUserDonation(Long id, User owner) {

        Donation donationToArchive = getUserDonationById(owner, id);
        archiveDonation(donationToArchive);
    }
}
