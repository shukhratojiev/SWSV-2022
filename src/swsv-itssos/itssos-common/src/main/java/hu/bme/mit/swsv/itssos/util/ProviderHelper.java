package hu.bme.mit.swsv.itssos.util;

import eu.arrowhead.client.library.util.ClientCommonConstants;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class ProviderHelper {
    @Value(ClientCommonConstants.$CLIENT_SYSTEM_NAME)
    private String mySystemName;

    @Value(ClientCommonConstants.$CLIENT_SERVER_ADDRESS_WD)
    private String mySystemAddress;

    @Value(ClientCommonConstants.$CLIENT_SERVER_PORT_WD)
    private int mySystemPort;

    public ServiceRegistryRequestDTO createServiceRegistryRequest(final String serviceDefinition, final String serviceUri, final HttpMethod httpMethod) {
        final ServiceRegistryRequestDTO serviceRegistryRequest = new ServiceRegistryRequestDTO();
        serviceRegistryRequest.setServiceDefinition(serviceDefinition);
        final SystemRequestDTO systemRequest = new SystemRequestDTO();
        systemRequest.setSystemName(mySystemName);
        systemRequest.setAddress(mySystemAddress);
        systemRequest.setPort(mySystemPort);

        serviceRegistryRequest.setSecure(ServiceSecurityType.NOT_SECURE.name());
        serviceRegistryRequest.setInterfaces(List.of(Constants.INTERFACE_INSECURE));
        serviceRegistryRequest.setProviderSystem(systemRequest);
        serviceRegistryRequest.setServiceUri(serviceUri);
        serviceRegistryRequest.setMetadata(new HashMap<>());
        serviceRegistryRequest.getMetadata().put(Constants.HTTP_METHOD, httpMethod.name());
        return serviceRegistryRequest;
    }
}
