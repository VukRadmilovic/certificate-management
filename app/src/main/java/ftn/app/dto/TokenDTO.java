package ftn.app.dto;

import lombok.Data;

@Data
public class TokenDTO {
    String accessToken;
    String refreshToken;

    public TokenDTO(String s, String s1) {
        this.accessToken = s;
        this.refreshToken = s1;
    }
}
