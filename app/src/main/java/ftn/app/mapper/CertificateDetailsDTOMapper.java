package ftn.app.mapper;

import ftn.app.dto.CertificateDetailsDTO;
import ftn.app.dto.CertificateRequestDetailsDTO;
import ftn.app.model.Certificate;
import ftn.app.model.CertificateRequest;
import ftn.app.model.enums.CertificateType;
import ftn.app.util.OrganizationDataUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CertificateDetailsDTOMapper {
    private static ModelMapper modelMapper;
    @Autowired
    public CertificateDetailsDTOMapper(ModelMapper modelMapper) {

        CertificateDetailsDTOMapper.modelMapper = modelMapper;
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STANDARD);
    }

    public static CertificateDetailsDTO fromCertificateToDTO(Certificate model) {
        CertificateDetailsDTO dto = modelMapper.map(model, CertificateDetailsDTO.class);
        if(model.getCertificateType() == CertificateType.ROOT)
        {
            dto.setCertificateType("ROOT");
        } else if (model.getCertificateType() == CertificateType.END) {
            dto.setCertificateType("END");
        } else if (model.getCertificateType() == CertificateType.INTERMEDIATE) {
            dto.setCertificateType("INTERMEDIATE");
        }
        dto.setOrganizationData(OrganizationDataUtils.parseOrganizationData(model.getOrganizationData()));
        return dto; }

    public static CertificateRequest fromDTOToCertificate(CertificateRequestDetailsDTO dto) { return modelMapper.map(dto,CertificateRequest.class); }

}
