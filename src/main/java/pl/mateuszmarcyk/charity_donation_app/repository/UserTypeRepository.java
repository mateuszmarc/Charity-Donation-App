package pl.mateuszmarcyk.charity_donation_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mateuszmarcyk.charity_donation_app.entity.UserType;

public interface UserTypeRepository extends JpaRepository<UserType, Long> {
}
