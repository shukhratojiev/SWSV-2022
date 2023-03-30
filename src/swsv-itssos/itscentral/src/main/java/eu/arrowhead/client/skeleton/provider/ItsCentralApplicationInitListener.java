package eu.arrowhead.client.skeleton.provider;

import eu.arrowhead.client.library.ArrowheadService;
import eu.arrowhead.client.library.config.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import hu.bme.mit.swsv.itssos.util.ProviderHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;

import static hu.bme.mit.swsv.itssos.itscentral.ItsCentralProviderConstants.REPORT_TRAIN_SERVICE_DEFINITION;
import static hu.bme.mit.swsv.itssos.itscentral.ItsCentralProviderConstants.REPORT_VEHICLE_SERVICE_DEFINITION;
import static hu.bme.mit.swsv.itssos.itscentral.ItsCentralProviderInternalConstants.REPORT_TRAIN_FULL_URI;
import static hu.bme.mit.swsv.itssos.itscentral.ItsCentralProviderInternalConstants.REPORT_VEHICLE_FULL_URI;
import static hu.bme.mit.swsv.itssos.util.Constants.INTERFACE_INSECURE;
import static hu.bme.mit.swsv.itssos.util.Endpoint.orchestrateAndRegisterEndpoint;
import static hu.bme.mit.swsv.itssos.vehicle.VehicleCommunicatorProviderConstants.SEND_NOTIFICATION_SERVICE_DEFINITION;
import static org.springframework.http.HttpMethod.POST;

@Component("ItsCentralApplicationInitListener")
public class ItsCentralApplicationInitListener extends ApplicationInitListener {

    //=================================================================================================
    // members

    @Autowired
    private ArrowheadService arrowheadService;

    @Autowired
    private ProviderHelper helper;

    @Autowired
    Environment environment;

    private final Logger logger = LogManager.getLogger(ItsCentralApplicationInitListener.class);

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Override
    protected void customInit(ContextRefreshedEvent event) {
        // tests should start the initialization manually
        if (!environment.acceptsProfiles(p -> p.test("test"))) {
            init(event.getApplicationContext());
        }
    }

    public void init(ApplicationContext applicationContext) {
        logger.info("ItsCentralApplicationInitListener custom init started");

        //Checking the availability of necessary core systems
        checkCoreSystemReachability(CoreSystem.SERVICE_REGISTRY);

        //Register services into ServiceRegistry
        final ServiceRegistryRequestDTO reportTrainServiceRequest =
                helper.createServiceRegistryRequest(REPORT_TRAIN_SERVICE_DEFINITION, REPORT_TRAIN_FULL_URI, POST);
        arrowheadService.forceRegisterServiceToServiceRegistry(reportTrainServiceRequest);
        final ServiceRegistryRequestDTO reportVehicleServiceRequest =
                helper.createServiceRegistryRequest(REPORT_VEHICLE_SERVICE_DEFINITION, REPORT_VEHICLE_FULL_URI, POST);
        arrowheadService.forceRegisterServiceToServiceRegistry(reportVehicleServiceRequest);

        // to consume vehicle-communicator
        checkCoreSystemReachability(CoreSystem.ORCHESTRATOR);
        // Initialize Arrowhead Context
        arrowheadService.updateCoreServiceURIs(CoreSystem.ORCHESTRATOR);

        @SuppressWarnings("unchecked")
        Map<String, Object> arrowheadContext = applicationContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
        orchestrateAndRegisterEndpoint(arrowheadService, arrowheadContext, SEND_NOTIFICATION_SERVICE_DEFINITION, INTERFACE_INSECURE, String.class);
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public void customDestroy() {
        //Unregister service
        arrowheadService.unregisterServiceFromServiceRegistry(REPORT_TRAIN_SERVICE_DEFINITION);
        arrowheadService.unregisterServiceFromServiceRegistry(REPORT_VEHICLE_SERVICE_DEFINITION);
    }

    //=================================================================================================
    // assistant methods
}
