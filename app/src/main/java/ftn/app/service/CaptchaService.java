package ftn.app.service;

import ftn.app.service.interfaces.ICaptchaService;
import ftn.app.util.CaptchaSettings;
import ftn.app.util.GoogleResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.management.BadAttributeValueExpException;
import java.net.URI;

@Service
public class CaptchaService implements ICaptchaService {
    private final CaptchaSettings captchaSettings;
    private final RestTemplate restTemplate;

    public CaptchaService(CaptchaSettings captchaSettings, RestTemplateBuilder restTemplateBuilder){
        restTemplate = restTemplateBuilder.build();
        this.captchaSettings = captchaSettings;
    }
    @Override
    public void processResponse(String response) throws BadAttributeValueExpException {
        URI verifyUri = URI.create(String.format(
                "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s&remoteip=%s",
                "6LdrNaImAAAAAImp6dtosVul9YzlLgVOSaHiFz4a", response, "localhost"));

        GoogleResponse googleResponse = restTemplate.getForObject(verifyUri, GoogleResponse.class);

        if(!googleResponse.isSuccess()) {
            throw new BadAttributeValueExpException("reCaptcha was not successfully validated");
        }
    }
}
