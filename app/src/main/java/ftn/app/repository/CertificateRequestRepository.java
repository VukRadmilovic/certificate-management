package ftn.app.repository;

import ftn.app.model.CertificateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRequestRepository extends JpaRepository<CertificateRequest, Integer> {

}
