package pl.mateuszmarcyk.charity_donation_app.donation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.mateuszmarcyk.charity_donation_app.user.User;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    @Query("SELECT COUNT(*) FROM Donation d")
    Integer countAll();

    @Query("SELECT SUM(d.quantity) FROM Donation  d")
    Integer countAllBags();

    @Query("SELECT d FROM Donation d WHERE d.user.id=:id ORDER BY d.created")
    List<Donation> findAllSortedByCreation(Long id);

    List<Donation> findAllDonationsByUser(User user);

    @Query(value = "SELECT d FROM Donation d WHERE d.user=:loggedUser ORDER BY d.created DESC")
    List<Donation> findAllDonationsByUserSortedByCreated(User loggedUser);

    @Query(value = "SELECT d FROM Donation d WHERE d.user=:loggedUser ORDER BY d.quantity DESC")
    List<Donation> findAllDonationByUserSortedByQuantityDesc(User loggedUser);

    @Query(value = "SELECT d FROM Donation d WHERE d.user=:loggedUser ORDER BY d.quantity ASC")
    List<Donation> findAllDonationByUserSortedByQuantityAsc(User loggedUser);

    @Query(value = "SELECT d FROM Donation d WHERE d.user=:loggedUser ORDER BY d.received ASC")
    List<Donation> findAllDonationByUserSortedByReceived(User loggedUser);

    @Query(value = "SELECT d FROM Donation d WHERE d.user=:loggedUser ORDER BY d.received DESC")
    List<Donation> findAllDonationByUserSortedByUnreceived(User loggedUser);
}
