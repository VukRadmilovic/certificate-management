package ftn.app.repository;

import ftn.app.model.CertificateRequest;
import ftn.app.model.User;
import ftn.app.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CertificateRequestRepository extends JpaRepository<CertificateRequest, Integer> {

    @Query(value = "SELECT request FROM CertificateRequest request LEFT JOIN Certificate cert ON request.issuerSerialNumber = cert.serialNumber WHERE cert.ownerEmail = :#{#user.email}")
    List<CertificateRequest> findByIssuerOwner(@Param("user") User user);

    List<CertificateRequest> findByIssuerSerialNumberAndRequestStatusEquals(String issuerSerialNumber, RequestStatus requestStatus);
}
