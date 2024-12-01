package pl.mateuszmarcyk.charity_donation_app.donation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    @Query("SELECT COUNT(*) FROM Donation d")
    Integer countAll();

    @Query("SELECT SUM(d.quantity) FROM Donation  d")
    Integer countAllBags();

    @Query("SELECT d FROM Donation d WHERE d.user.id=:id ORDER BY d.created")
    List<Donation> findAllSortedByCreation(Long id);
}
