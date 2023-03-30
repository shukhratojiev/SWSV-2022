package hu.bme.mit.swsv.itssos.util;

import eu.arrowhead.client.library.ArrowheadService;
import eu.arrowhead.common.dto.shared.*;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;

import java.util.Map;
import java.util.NoSuchElementException;

import static hu.bme.mit.swsv.itssos.util.Utils.bind;
import static hu.bme.mit.swsv.itssos.util.Utils.toPrettyJson;

public class Endpoint<T> {
    private static final Logger logger = LogManager.getLogger(Endpoint.class);

    private ArrowheadService arrowheadService;
    private HttpMethod httpMethod;
    private String address;
    private int port;
    private String serviceUri;
    private String interfaceName;
    private Class<T> responseType;
    // the serviceDefinition used in orchestration (case might differ)
    private String serviceDefinition;

    private Endpoint(ArrowheadService arrowheadService, HttpMethod httpMethod, String address, int port, String serviceUri, String interfaceName, Class<T> responseType, String serviceDefinition) {
        this.arrowheadService = arrowheadService;
        this.httpMethod = httpMethod;
        this.address = address;
        this.port = port;
        this.serviceUri = serviceUri;
        this.interfaceName = interfaceName;
        this.responseType = responseType;
        this.serviceDefinition = serviceDefinition;
    }

    private static void validateOrchestrationResult(OrchestrationResultDTO orchestrationResult, String serviceDefinition, String requiredInterface) {
        if (!orchestrationResult.getService().getServiceDefinition().equalsIgnoreCase(serviceDefinition)) {
            throw new InvalidParameterException("Requested and orchestrated service definition do not match");
        }

        for (final ServiceInterfaceResponseDTO serviceInterface : orchestrationResult.getInterfaces()) {
            if (serviceInterface.getInterfaceName().equalsIgnoreCase(requiredInterface)) {
                return;
            }
        }

        throw new InvalidParameterException("Requested and orchestrated interface do not match");
    }

    private static OrchestrationResultDTO orchestrate(ArrowheadService arrowheadService, String serviceDefinition, String requiredInterface) {
        logger.info("Orchestration request for {} service:", serviceDefinition);
        final ServiceQueryFormDTO serviceQueryForm = new ServiceQueryFormDTO.Builder(serviceDefinition)
                .interfaces(requiredInterface)
                .build();

        final OrchestrationFormRequestDTO orchestrationFormRequest = arrowheadService.getOrchestrationFormBuilder()
                .requestedService(serviceQueryForm)
                .flag(OrchestrationFlags.Flag.MATCHMAKING, true) // When this flag is false or not specified, then the orchestration response cloud contain more proper provider. Otherwise only one will be chosen if there is any proper.
                .flag(OrchestrationFlags.Flag.OVERRIDE_STORE, true) // When this flag is false or not specified, then a Store Orchestration will be proceeded. Otherwise a Dynamic Orchestration will be proceeded.
                .build();

        logger.info(() -> toPrettyJson(orchestrationFormRequest));

        OrchestrationResponseDTO orchestrationResponse;
        try {
            orchestrationResponse = arrowheadService.proceedOrchestration(orchestrationFormRequest);
        } catch (final ArrowheadException ex) {
            logger.error(ex);
            throw ex;
        }

        logger.info(bind(o -> "Orchestration response:\n" + toPrettyJson(o), orchestrationResponse));

        if (orchestrationResponse == null || orchestrationResponse.getResponse().isEmpty()) {
            logger.error("Orchestration response is empty");
            throw new ArrowheadException("Orchestration response is empty");
        }

        final OrchestrationResultDTO orchestrationResult = orchestrationResponse.getResponse().get(0); //Simplest way of choosing a provider.
        validateOrchestrationResult(orchestrationResult, serviceDefinition, requiredInterface);

        return orchestrationResult;
    }

    public static <T> Endpoint<T> orchestrateEndpoint(ArrowheadService arrowheadService, String serviceDefinition, String requiredInterface, Class<T> responseType) {
        OrchestrationResultDTO orchestrationResult = orchestrate(arrowheadService, serviceDefinition, requiredInterface);

        return new Endpoint<>(arrowheadService,
                HttpMethod.valueOf(orchestrationResult.getMetadata().get(Constants.HTTP_METHOD)),
                orchestrationResult.getProvider().getAddress(),
                orchestrationResult.getProvider().getPort(),
                orchestrationResult.getServiceUri(),
                orchestrationResult.getInterfaces().get(0).getInterfaceName(), // Simplest way of choosing an interface.
                responseType,
                serviceDefinition
        );
    }

    public static <T> Endpoint<T> orchestrateAndRegisterEndpoint(ArrowheadService arrowheadService, Map<String, Object> arrowheadContext,
                                                                 String serviceDefinition, String requiredInterface, Class<T> responseType) {
        Endpoint<T> endpoint = orchestrateEndpoint(arrowheadService, serviceDefinition, requiredInterface, responseType);
        registerEndpoint(arrowheadContext, endpoint);
        return endpoint;
    }

    public static final String ENDPOINT_SUFFIX = "-" + Endpoint.class.getCanonicalName();

    private static <T> void registerEndpoint(Map<String, Object> arrowheadContext, Endpoint<T> endpoint) {
        arrowheadContext.put(endpoint.serviceDefinition + ENDPOINT_SUFFIX, endpoint);
    }

    public static <T> Endpoint<T> getEndpoint(Map<String, Object> arrowheadContext, String serviceDefinition) {
        @SuppressWarnings("unchecked")
        Endpoint<T> endpoint = (Endpoint<T>) arrowheadContext.get(serviceDefinition + ENDPOINT_SUFFIX);
        if (endpoint == null) {
            throw new NoSuchElementException("Endpoint '" + serviceDefinition + "' is not found. Have you run orchestrateAndRegisterEndpoint?");
        }

        return endpoint;
    }

    public T consumeServiceHTTP(Object payload, String... queryParams) {
        logger.info(() -> "Request:\n" + toPrettyJson(payload));
        T response = arrowheadService.consumeServiceHTTP(responseType, httpMethod, address, port, serviceUri, interfaceName, null, payload, queryParams);
        logger.info(() -> "Provider response:\n" + toPrettyJson(response));

        return response;
    }
}
