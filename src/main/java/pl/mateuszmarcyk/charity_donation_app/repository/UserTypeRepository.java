package pl.mateuszmarcyk.charity_donation_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.mateuszmarcyk.charity_donation_app.entity.UserType;

import java.util.Optional;

public interface UserTypeRepository extends JpaRepository<UserType, Long> {

    @Query("SELECT ut FROM UserType  ut WHERE ut.role=:admin")
    Optional<UserType> findByRole(String admin);
}
