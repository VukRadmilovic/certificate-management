package ftn.app.service.interfaces;

public interface IMailService {
    public void sendSimpleMessage(String to, String subject, String text);
}
