package ftn.app.repository;

import ftn.app.model.Role;
import ftn.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}
