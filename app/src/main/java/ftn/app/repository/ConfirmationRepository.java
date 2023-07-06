package ftn.app.repository;

import ftn.app.model.Certificate;
import ftn.app.model.Confirmation;
import ftn.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConfirmationRepository extends JpaRepository<Confirmation, Integer> {
    List<Confirmation> findAllByUserAndExpired(User user, Boolean expired);
}
