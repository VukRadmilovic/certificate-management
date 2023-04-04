package ftn.app.mapper;

import ftn.app.dto.CertificateRequestDTO;
import ftn.app.dto.UserFullDTO;
import ftn.app.model.CertificateRequest;
import ftn.app.model.User;
import ftn.app.util.OrganizationDataUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserFullDTOMapper {
    private static ModelMapper modelMapper;
    @Autowired
    public UserFullDTOMapper(ModelMapper modelMapper) {
        UserFullDTOMapper.modelMapper = modelMapper;
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STANDARD);
    }
    public static UserFullDTO fromUserToDTO(User model) { return modelMapper.map(model, UserFullDTO.class); }
    public static User fromDTOToUser(UserFullDTO dto) {
        return modelMapper.map(dto,User.class);
    }
}
