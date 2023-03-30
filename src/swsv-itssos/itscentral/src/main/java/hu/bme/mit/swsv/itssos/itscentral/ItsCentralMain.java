package hu.bme.mit.swsv.itssos.itscentral;

import eu.arrowhead.common.CommonConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {CommonConstants.BASE_PACKAGE, ItsCentralProviderInternalConstants.BASE_PACKAGE})
public class ItsCentralMain {

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public static void main(final String[] args) {
        SpringApplication.run(ItsCentralMain.class, args);
    }
}
