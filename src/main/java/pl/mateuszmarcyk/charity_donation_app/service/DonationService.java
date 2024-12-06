package pl.mateuszmarcyk.charity_donation_app.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.repository.DonationRepository;
import pl.mateuszmarcyk.charity_donation_app.entity.Donation;
import pl.mateuszmarcyk.charity_donation_app.util.event.DonationProcessCompleteEvent;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.entity.Institution;
import pl.mateuszmarcyk.charity_donation_app.entity.User;

import java.time.LocalDateTime;
import java.util.Comparator;
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

    public List<Donation> findAll(String sortType) {
        List<Donation> allDonations =  donationRepository.findAll();
        if (sortType == null) {
            return allDonations;
        } else if (sortType.equals("created")) {
             allDonations.sort(Comparator.comparing(Donation::getCreated));
        } else if (sortType.equals("quantity desc")) {
            allDonations.sort(Comparator.comparing(Donation::getQuantity).reversed());
        } else if (sortType.equals("quantity asc")) {
            allDonations.sort(Comparator.comparing(Donation::getQuantity));
        } else if (sortType.equals("received asc")) {
            allDonations.sort(Comparator.comparing(Donation::isReceived));
        }
        return allDonations;
    }

    @Transactional
    public void unarchiveDonation(Donation donationToArchive) {
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
}
