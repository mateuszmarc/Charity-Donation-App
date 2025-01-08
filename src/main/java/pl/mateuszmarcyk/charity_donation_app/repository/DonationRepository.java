package pl.mateuszmarcyk.charity_donation_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.mateuszmarcyk.charity_donation_app.entity.Donation;
import pl.mateuszmarcyk.charity_donation_app.entity.User;

import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    @Query("SELECT COUNT(*) FROM Donation d")
    Integer countAll();

    @Query("SELECT SUM(d.quantity) FROM Donation  d")
    Integer countAllBags();

    @Query("SELECT d FROM Donation d WHERE d.user.id=:id ORDER BY d.created")
    List<Donation> findAllDonationsByUserIdSortedByCreation(Long id);

    List<Donation> findAllDonationsByUser(User user);

    @Query(value = "SELECT d FROM Donation d WHERE d.user=:loggedUser ORDER BY d.created DESC")
    List<Donation> findAllDonationsByUserSortedByCreated(User loggedUser);

    @Query(value = "SELECT d FROM Donation d WHERE d.user=:loggedUser ORDER BY d.quantity DESC")
    List<Donation> findAllDonationsByUserSortedByQuantityDesc(User loggedUser);

    @Query(value = "SELECT d FROM Donation d WHERE d.user=:loggedUser ORDER BY d.quantity ASC")
    List<Donation> findAllDonationsByUserSortedByQuantityAsc(User loggedUser);

    @Query(value = "SELECT d FROM Donation d WHERE d.user=:loggedUser ORDER BY d.received ASC")
    List<Donation> findAllDonationsByUserSortedByReceived(User loggedUser);

    @Query(value = "SELECT d FROM Donation d WHERE d.user=:loggedUser ORDER BY d.received DESC")
    List<Donation> findAllDonationsByUserSortedByUnreceived(User loggedUser);

//    method to test
    @Query("SELECT d FROM Donation d ORDER BY d.created DESC")
    List<Donation> findAllDonationsSortedByCreated();

    @Query("SELECT d FROM Donation d ORDER BY d.quantity DESC")
    List<Donation> findAllDonationsByQuantityDesc();

    @Query("SELECT d FROM Donation d ORDER BY d.received ASC")
    List<Donation> findAllDonationsSortedByReceivedAsc();

    @Query("SELECT d FROM Donation d ORDER BY d.quantity ASC")
    List<Donation> findAllDonationsSortedByQuantityAsc();

    @Query("SELECT d FROM Donation d WHERE d.user =:owner AND d.id=:id")
    Optional<Donation> findUserDonationById(User owner, Long id);
}
