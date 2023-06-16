package ftn.app.util;

import ftn.app.model.enums.EventType;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

@Component
public class KeystoreUtils {

    private KeyStore keyStore;
    private final String KEYSTORE_PATH = "src/main/resources/keystore/keystore.jks";
    private final char[] KEYSTORE_PASSWORD = "tim21KSSec".toCharArray();

    public KeystoreUtils() {
        try {
            keyStore = KeyStore.getInstance("JKS", "SUN");
        } catch (KeyStoreException | NoSuchProviderException e) {
            LoggingUtil.LogEvent("Internal error.", EventType.ERROR, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Omogucava koriscenje postojeceg keystore-a
     */
    public void loadKeystore() {
        try {
            keyStore.load(new FileInputStream(KEYSTORE_PATH), KEYSTORE_PASSWORD);
        } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            LoggingUtil.LogEvent("Internal error.", EventType.ERROR, e.getMessage());
            e.printStackTrace();
        }
    }

    /*NE POZIVATI UKOLIKO POSTOJI KREIRAN KEYSTORE.JKS*/

    /**
     * Klasa koja pravi novi keystore.
     * NE POZIVATI UKOLIKO POSTOJI VEC KREIRAN KEYSTORE.JKS U KEYSTORES FOLDERU!
     */
    public void saveKeystore() {
        try {
            keyStore.load(null, KEYSTORE_PASSWORD);
            keyStore.store(new FileOutputStream(KEYSTORE_PATH), KEYSTORE_PASSWORD);

        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            LoggingUtil.LogEvent("Internal error.", EventType.ERROR, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cuva sertifikat i privatni kljuc koji se generise za njega
     * @param alias - jedinstvena oznaka za sertifikat/privatni kljuc koji se cuva u keystore-u
     * @param privateKey - privatni kljuc koji odgovara sertifikatu i cuva se pod alijasom aliasKey
     * @param password - sifra pod kojom se cuva privatni kljuc
     * @param certificate - izgenerisan sertifikat koji se cuva pod alijasom aliasCert
     */
    public void saveCertificate(String alias, PrivateKey privateKey, char[] password, Certificate certificate) {
        try {
            loadKeystore();
            keyStore.setCertificateEntry(alias + "Cert", certificate);
            keyStore.setKeyEntry(alias + "Key", privateKey, password, new Certificate[]{certificate});
            keyStore.store(new FileOutputStream(KEYSTORE_PATH), KEYSTORE_PASSWORD); // Save the modified keystore back to the file
        } catch (KeyStoreException e) {
            LoggingUtil.LogEvent("Internal error.", EventType.ERROR, e.getMessage());
            e.printStackTrace();
        } catch (CertificateException | IOException | NoSuchAlgorithmException e) {
            LoggingUtil.LogEvent("Internal error.", EventType.ERROR, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Cita sertifikat iz keystore-a
     * @param alias - alias pod kojim se cuva bez Cert sufiksa
     * @return sertifikat pod zadatim alijasom
     */
    public Certificate readCertificate(String alias) {
        try {
            loadKeystore();
            alias = alias + "Cert";
            if (keyStore.isCertificateEntry(alias)) {
                return keyStore.getCertificate(alias);
            }
        } catch (KeyStoreException e) {
            LoggingUtil.LogEvent("Internal error.", EventType.ERROR, e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Cita privatni kljuc koji se vezuje za odredjeni sertifikat
     * @param alias - alias pod kojim se cuva kljuc bez sufiksa Key
     * @param pass - lozinka pod kojom se cuva privatni kljuc (nije isto sto i lozinka za keystore!!)
     * @return privatni kljuc pod zadatim alijasom i lozinkom
     */
    public PrivateKey readPrivateKey(String alias, String pass) {
        try {
            alias = alias + "Key";
            if (keyStore.isKeyEntry(alias)) {
                return (PrivateKey) keyStore.getKey(alias, pass.toCharArray());
            }
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            LoggingUtil.LogEvent("Internal error.", EventType.ERROR, e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
