package ftn.app.dto;

import lombok.Data;

@Data
public class TokenDTO {
    String accessToken;

    public TokenDTO(String accessToken) {
        this.accessToken = accessToken;
    }
}
