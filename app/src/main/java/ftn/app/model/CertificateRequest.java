package ftn.app.model;

import ftn.app.model.enums.CertificateType;
import ftn.app.model.enums.RequestStatus;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "certificate_requests")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique=true, nullable=false)
    private Integer id;

    @Column()
    private String issuerSerialNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private User requester;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CertificateType certificateType;

    @Column(nullable = false)
    private Date dateRequested;

    @Column(nullable = false)
    private Date validUntil;

    @Column(nullable = false)
    private String organizationData;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    @Column()
    private String denialReason;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CertificateRequest certificateRequest = (CertificateRequest) o;
        return id != null && Objects.equals(id, certificateRequest.id);
    }

}
