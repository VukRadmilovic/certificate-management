package ftn.app.keystoreUtils;

import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

@Component
public class KeystoreUtils {

    private KeyStore keyStore;
    private final String KEYSTORE_PATH = "keystores/keystore.jks";
    private final char[] KEYSTORE_PASSWORD = "tim21KSSec".toCharArray();

    public KeystoreUtils() {
        try {
            keyStore = KeyStore.getInstance("JKS", "SUN");
        } catch (KeyStoreException | NoSuchProviderException e) {
            e.printStackTrace();
        }
    }
    private void loadKeystore() {
        try {
            keyStore.load(new FileInputStream(KEYSTORE_PATH), KEYSTORE_PASSWORD);
        } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            e.printStackTrace();
        }
    }

    public void saveKeystore() {
        try {
            keyStore.load(null, KEYSTORE_PASSWORD);
            keyStore.store(new FileOutputStream(KEYSTORE_PATH), KEYSTORE_PASSWORD);

        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCertificate(String alias, PrivateKey privateKey, char[] password, Certificate certificate) {
        try {
            loadKeystore();
            keyStore.setCertificateEntry(alias + "Cert", certificate);
            keyStore.setKeyEntry(alias + "Key", privateKey, password, new Certificate[]{certificate});
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

    public Certificate readCertificate(String alias) throws KeyStoreException {
        try {
            alias = alias + "Cert";
            if (keyStore.isCertificateEntry(alias)) {
                return keyStore.getCertificate(alias);
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }


    public PrivateKey readPrivateKey(String alias, String pass) {
        try {
            alias = alias + "Key";
            if (keyStore.isKeyEntry(alias)) {
                return (PrivateKey) keyStore.getKey(alias, pass.toCharArray());
            }
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        return null;
    }
}
