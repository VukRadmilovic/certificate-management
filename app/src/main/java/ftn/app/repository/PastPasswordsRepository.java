package ftn.app.repository;

import ftn.app.model.UserPastPasswords;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PastPasswordsRepository extends JpaRepository<UserPastPasswords,Integer> {

    Optional<List<UserPastPasswords>> getUserPastPasswordsByEmail(String email);
}
