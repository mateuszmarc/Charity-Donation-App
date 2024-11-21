package pl.mateuszmarcyk.charity_donation_app.userprofile;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
