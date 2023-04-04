package ftn.app.service.interfaces;

import ftn.app.dto.CertificateDetailsDTO;
import ftn.app.dto.CertificateRequestDTO;
import ftn.app.dto.CertificateRequestDetailsDTO;
import ftn.app.model.Certificate;
import ftn.app.model.CertificateRequest;
import ftn.app.model.User;

import java.util.List;

public interface ICertificateService {

    CertificateRequestDetailsDTO requestCertificate(CertificateRequestDTO requestDTO, User requester);
    CertificateRequest saveRequest(CertificateRequestDTO requestDTO, User requester, Certificate issuer);
    Certificate saveCertificate(CertificateRequestDTO requestDTO, User requester);
    boolean isValidCertificate(Certificate certificate, boolean isOverallValid);
    List<CertificateDetailsDTO> getAllCertificates();

    List<CertificateRequestDetailsDTO> getUserRequests(User user);

    List<CertificateRequestDetailsDTO> getAllRequests();
}
