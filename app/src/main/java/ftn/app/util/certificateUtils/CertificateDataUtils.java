package ftn.app.util.certificateUtils;

import ftn.app.model.IssuerData;
import ftn.app.model.SubjectData;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.springframework.stereotype.Component;

import java.security.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class CertificateDataUtils {

    /**
     * Kreira informacije o samom sertifikatu koje se koriste za generisanje sertifikata
     * @return SubjectData objekat sa odgovarajucim informacijama
     */
    public SubjectData generateSubjectData() {
        try {
            KeyPair keyPairSubject = generateKeyPair();

            SimpleDateFormat iso8601Formater = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = iso8601Formater.parse("2022-03-01");
            Date endDate = iso8601Formater.parse("2024-03-01");
            String sn = "1";

            X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
            builder.addRDN(BCStyle.CN, "Dracooya");
            builder.addRDN(BCStyle.SURNAME, "Varga");
            builder.addRDN(BCStyle.GIVENNAME, "Maja");
            builder.addRDN(BCStyle.O, "UNS-FTN");
            builder.addRDN(BCStyle.OU, "Katedra za informatiku");
            builder.addRDN(BCStyle.C, "RS");
            builder.addRDN(BCStyle.E, "maja.varga@uns.ac.rs");
            builder.addRDN(BCStyle.UID, "654321");
            return new SubjectData(keyPairSubject.getPublic(), builder.build(), sn, startDate, endDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Kreira informacije o izdavacu sertifikata koje se koriste za generisanje sertifikata
     * @return IssuerData objekat sa odgovarajucim informacijama
     */
    public IssuerData generateIssuerData() {
        KeyPair issuerKey = generateKeyPair();
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        builder.addRDN(BCStyle.CN, "Goran Sladic");
        builder.addRDN(BCStyle.SURNAME, "Sladic");
        builder.addRDN(BCStyle.GIVENNAME, "Goran");
        builder.addRDN(BCStyle.O, "UNS-FTN");
        builder.addRDN(BCStyle.OU, "Katedra za informatiku");
        builder.addRDN(BCStyle.C, "RS");
        builder.addRDN(BCStyle.E, "sladicg@uns.ac.rs");
        builder.addRDN(BCStyle.UID, "123456");

        return new IssuerData(builder.build(),issuerKey.getPrivate());
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
