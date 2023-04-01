package ftn.app.util.certificateUtils;

import ftn.app.model.IssuerData;
import ftn.app.model.SubjectData;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Component
public class CertificateUtils {

    /**
     * Generise sertifikat koji se moze cuvati u keystore-u
     * @param subjectData - informacije o samom sertifikatu
     * @param issuerData - informacije o izdavacu sertifikata
     * @return izgenerisan sertifikat
     */
    public X509Certificate generateCertificate(SubjectData subjectData, IssuerData issuerData) {
        try {
            JcaContentSignerBuilder builder = new JcaContentSignerBuilder("SHA256WithRSAEncryption");
            builder = builder.setProvider("BC");
            ContentSigner contentSigner = builder.build(issuerData.getPrivateKey());

            X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(
                    issuerData.getX500name(),
                    new BigInteger(subjectData.getSerialNumber()),
                    subjectData.getStartDate(),
                    subjectData.getEndDate(),
                    subjectData.getX500name(),
                    subjectData.getPublicKey());

            X509CertificateHolder certHolder = certGen.build(contentSigner);
            JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
            certConverter = certConverter.setProvider("BC");
            return certConverter.getCertificate(certHolder);

        } catch (IllegalArgumentException | IllegalStateException | OperatorCreationException | CertificateException e) {
            e.printStackTrace();
        }
        return null;
    }
}
