package ftn.app.util;

import ftn.app.model.OrganizationData;
import org.springframework.stereotype.Component;

@Component
public class OrganizationDataUtils {

    public static OrganizationData parseOrganizationData(String organizationData){
        String[] tokens = organizationData.split("\\|");
        return new OrganizationData(tokens[0],tokens[1],tokens[2]);
    }

    public static String writeOrganizationData(OrganizationData data){
         return data.getName() + "|" + data.getUnit() + "|" + data.getCountryCode();
    }

}
