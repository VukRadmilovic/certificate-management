package ftn.app.service.interfaces;

import ftn.app.dto.CertificateRequestDTO;
import ftn.app.dto.CertificateRequestDetailsDTO;
import ftn.app.model.User;

import java.util.List;

public interface ICertificateRequestService {
    CertificateRequestDetailsDTO createRequest(CertificateRequestDTO requestDTO, User requester);
    CertificateRequestDetailsDTO acceptRequest(Integer requestId, User user);
    CertificateRequestDetailsDTO denyRequest(Integer requestId, String reason, User user);
    List<CertificateRequestDetailsDTO> getUserRequests(User user);
    List<CertificateRequestDetailsDTO> getReceivedRequests(User user);

    List<CertificateRequestDetailsDTO> getAllRequests();
}
