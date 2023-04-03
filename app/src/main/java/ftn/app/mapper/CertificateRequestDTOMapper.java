package ftn.app.mapper;

import ftn.app.dto.CertificateRequestDTO;
import ftn.app.model.CertificateRequest;
import ftn.app.model.OrganizationData;
import ftn.app.util.OrganizationDataUtils;
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

    }

    public static CertificateRequestDTO fromRequestToDTO(CertificateRequest model) { return modelMapper.map(model, CertificateRequestDTO.class); }

    public static CertificateRequest fromDTOToRequest(CertificateRequestDTO dto) {
        CertificateRequest request = modelMapper.map(dto,CertificateRequest.class);
        request.setOrganizationData(OrganizationDataUtils.writeOrganizationData(dto.getOrganizationData()));
        return request;
    }

}
