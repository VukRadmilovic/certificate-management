package ftn.app.util.certificateUtils;

import ftn.app.model.IssuerData;
import ftn.app.model.OrganizationData;
import ftn.app.model.SubjectData;
import ftn.app.model.User;
import ftn.app.repository.CertificateRepository;
import ftn.app.util.DateUtil;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.springframework.stereotype.Component;

import java.security.*;
import java.util.Date;
import java.util.Random;

@Component
public class CertificateDataUtils {

    private final CertificateRepository certificateRepository;

    public CertificateDataUtils(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    /**
     * Kreira informacije o samom sertifikatu koje se koriste za generisanje sertifikata
     * @param requestMaker - korisnik koji zahteva sertifikat
     * @param organizationData - podaci o organizaciji korisnika koji zahteva sertifikat
     * @param validUntil - datum do kog vazi sertifikat
     * @return SubjectData objekat sa odgovarajucim informacijama
     */
    public SubjectData generateSubjectData(User requestMaker, OrganizationData organizationData, Date validUntil) {
        KeyPair keyPairSubject = generateKeyPair();
        String serialNumber = generateSerialNumber();
        return new SubjectData(keyPairSubject.getPublic(),
                generateX500Name(requestMaker,organizationData),
                serialNumber,
                DateUtil.getDateWithoutTime(new Date()),
                validUntil);
    }

    /***
     * Generise X500 ime korisnika koji zahteva izdavanje sertifikata/ korisnika koji je vlasnik sertifikata koji ga potpisuje
     * @param user - konkretan korisnik
     * @param organizationData - podaci o organizaciji korisnika
     * @return X500 ime korisnika (koristi se za generisanje sertifikata)
     */
    private X500Name generateX500Name(User user, OrganizationData organizationData){
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        builder.addRDN(BCStyle.CN, user.getName() + " " + user.getSurname());
        builder.addRDN(BCStyle.SURNAME, user.getSurname());
        builder.addRDN(BCStyle.GIVENNAME, user.getName());
        builder.addRDN(BCStyle.O, organizationData.getName());
        builder.addRDN(BCStyle.OU, organizationData.getUnit());
        builder.addRDN(BCStyle.C, organizationData.getCountryCode());
        builder.addRDN(BCStyle.E, user.getName());
        builder.addRDN(BCStyle.UID, user.getId().toString());
        return builder.build();
    }

    /**
     * Generise jedinstven serijski broj sertifikata
     * @return jednistven serijski broj sertifikata (serialNumber)
     */
    private String generateSerialNumber() {
        String sn = "";
        do {
            sn = Integer.toString(new Random().nextInt(1000000) + 1);
        } while (certificateRepository.findBySerialNumber(sn).isPresent());
        return sn;
    }

    /**
     * Kreira informacije o izdavacu sertifikata koje se koriste za generisanje sertifikata
     * @param issuer - korisnik koji je vlasnik sertifikata koji treba da potpise neki drugi sertifikat
     * @param organizationData - podaci o organizaciji issuer-a
     * @return IssuerData objekat sa odgovarajucim informacijama
     */
    public IssuerData generateIssuerData(User issuer, OrganizationData organizationData) {
        KeyPair issuerKey = generateKeyPair();
        return new IssuerData(generateX500Name(issuer,organizationData),issuerKey.getPrivate());
    }

    /**
     * Generise par public/private key
     * @return par od kojeg je moguce preko .getPrivate() i .getPublic() dobiti pojedinacne kljuceve
     */
    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(2048, random);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }
}
