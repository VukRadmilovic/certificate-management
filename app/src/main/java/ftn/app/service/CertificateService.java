package ftn.app.service;

import ftn.app.repository.CertificateRepository;
import ftn.app.service.interfaces.ICertificateService;
import org.springframework.stereotype.Service;

@Service
public class CertificateService implements ICertificateService {

    private final CertificateRepository certificateRepository;

    public CertificateService(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }
}
