package ftn.app.model;

import lombok.*;
import org.modelmapper.internal.bytebuddy.agent.builder.AgentBuilder;

import javax.persistence.*;

@Entity
@Table(name = "Confirmation")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Confirmation {
    @Id
    @Column(unique = true,nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private User user;

    @Column(nullable = false)
    private String confirmation;

    @Column(nullable = false)
    private Boolean expired;
}
