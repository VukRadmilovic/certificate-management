package ftn.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.io.Serial;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="ROLE")
public class Role implements GrantedAuthority {
	@Serial
    private static final long serialVersionUID = 1L;
	@Id
    @Column(unique = true,nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    @JsonIgnore
    @Override
    public String getAuthority() {
        return name;
    }

    public String getName() {
        return name;
    }
}
