package pl.mateuszmarcyk.charity_donation_app.donation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    @Query("SELECT COUNT(d) FROM Donation d")
    public long countAll();

    @Query("SELECT SUM(d.quantity) FROM Donation  d")
    public long countAllBags();
}
