package ftn.app.mapper;

import ftn.app.dto.CertificateRequestDTO;
import ftn.app.model.CertificateRequest;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CertificateRequestDTOMapper {
    private static ModelMapper modelMapper;
    @Autowired
    public CertificateRequestDTOMapper(ModelMapper modelMapper) {

        CertificateRequestDTOMapper.modelMapper = modelMapper;
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STANDARD);

        modelMapper.typeMap(CertificateRequest.class, CertificateRequestDTO.class).addMappings(mapper -> {
            mapper.map(CertificateRequest::getOrganizationData,
                    CertificateRequestDTO::generateOrganizationData);
        });

        modelMapper.typeMap(CertificateRequestDTO.class, CertificateRequest.class).addMappings(mapper -> {
            mapper.map(CertificateRequestDTO::getOrganizationData,
                    CertificateRequest::generateOrganizationData);
        });
    }

    public static CertificateRequestDTO fromRequestToDTO(CertificateRequest model) { return modelMapper.map(model, CertificateRequestDTO.class); }

    public static CertificateRequest fromDTOToRequest(CertificateRequestDTO dto) { return modelMapper.map(dto,CertificateRequest.class); }

}
