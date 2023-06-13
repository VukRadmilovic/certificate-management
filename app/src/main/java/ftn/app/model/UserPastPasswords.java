package ftn.app.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "usersPastPasswords")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserPastPasswords {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique=true, nullable=false)
    private Integer id;

    @Column (nullable = false)
    private String email;

    @Column (nullable = false)
    private String password;


    public UserPastPasswords(String email, String password) {
        this.email = email;
        this.password = password;
    }

}
