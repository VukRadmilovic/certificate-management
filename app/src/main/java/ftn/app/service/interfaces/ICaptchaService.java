package ftn.app.service.interfaces;

import javax.management.BadAttributeValueExpException;

public interface ICaptchaService {
    public void processResponse(String response) throws BadAttributeValueExpException;
}
