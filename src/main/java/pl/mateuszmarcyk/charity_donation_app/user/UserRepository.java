package pl.mateuszmarcyk.charity_donation_app.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(@NotNull @Email String email);

    Optional<User> findUserByVerificationToken_Token(String token);

    @Query(value = "SELECT u.* FROM users u " +
            "JOIN users_user_types uut ON u.id = uut.user_id " +
            "JOIN user_types ut ON uut.user_type_id = ut.id " +
            "WHERE ut.role = :role", nativeQuery = true)
    List<User> findUsersByRoleNative(@Param("role") String role);
}
