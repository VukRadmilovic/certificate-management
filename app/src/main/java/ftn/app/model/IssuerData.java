package ftn.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bouncycastle.asn1.x500.X500Name;

import java.security.PrivateKey;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssuerData {

	private X500Name x500name;
	private PrivateKey privateKey;

}
