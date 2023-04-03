package ftn.app.mapper;

import ftn.app.dto.CertificateRequestDTO;
import ftn.app.dto.CertificateRequestDetailsDTO;
import ftn.app.model.CertificateRequest;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CertificateRequestDetailsDTOMapper {

    private static ModelMapper modelMapper;
    @Autowired
    public CertificateRequestDetailsDTOMapper(ModelMapper modelMapper) {

        CertificateRequestDetailsDTOMapper.modelMapper = modelMapper;
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STANDARD);
    }

    public static CertificateRequestDetailsDTO fromRequestToDTO(CertificateRequest model) {
        CertificateRequestDetailsDTO dto = modelMapper.map(model, CertificateRequestDetailsDTO.class);
        dto.getRequester().setFullName(model.getRequester().getName() + " " + model.getRequester().getSurname());
        dto.generateOrganizationData(model.getOrganizationData());
        return dto; }

    public static CertificateRequest fromDTOToRequest(CertificateRequestDetailsDTO dto) { return modelMapper.map(dto,CertificateRequest.class); }
}
