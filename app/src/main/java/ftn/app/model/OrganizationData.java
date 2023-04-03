package ftn.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationData {

    @NotBlank
    @Length(max=255, message = "{maxLength}")
    private String name;
    @NotBlank
    @Length(max=255, message = "{maxLength}")
    private String unit;

    @NotBlank
    @Length(min = 3, max = 3, message = "{countryCode}")
    private String countryCode;
}
