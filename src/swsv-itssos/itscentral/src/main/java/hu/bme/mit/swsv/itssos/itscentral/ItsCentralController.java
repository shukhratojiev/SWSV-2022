package hu.bme.mit.swsv.itssos.itscentral;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.exception.BadPayloadException;
import hu.bme.mit.swsv.itssos.itscentral.logic.ItsCentralLogic;
import hu.bme.mit.swsv.itssos.itscentral.logic.NotificationLevel;
import hu.bme.mit.swsv.itssos.itscentral.logic.VehicleCommunicator;
import hu.bme.mit.swsv.itssos.itscentral.logic.impl.ItsCentralLogicImpl;
import hu.bme.mit.swsv.itssos.util.Endpoint;
import hu.bme.mit.swsv.itssos.vehicle.SendNotificationRequestDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

import static hu.bme.mit.swsv.itssos.vehicle.VehicleCommunicatorProviderConstants.SEND_NOTIFICATION_SERVICE_DEFINITION;

@RestController
@DependsOn({"ItsCentralApplicationInitListener"})
@RequestMapping(ItsCentralProviderInternalConstants.ITSCENTRAL_URI)
public class ItsCentralController {

    //=================================================================================================
    // members
    @Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
    private Map<String, Object> arrowheadContext;

    @Autowired
    Environment environment;

    private Logger logger = LogManager.getLogger(ItsCentralController.class);
    private ItsCentralLogic itsCentralLogic;

    //=================================================================================================
    // methods

    // after ItsCentralApplicationInitListener.customInit
    @EventListener
    @Order(11)
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // tests should start the initialization manually
        if (!environment.acceptsProfiles(p -> p.test("test"))) {
            init();
        }
    }

    protected void init() {
        VehicleCommunicator vehicleCommunicator = new VehicleCommunicator() {
            @Override
            public void sendBroadcastNotification(NotificationLevel level) {
                Endpoint.getEndpoint(arrowheadContext, SEND_NOTIFICATION_SERVICE_DEFINITION)
                        .consumeServiceHTTP(new SendNotificationRequestDto(null, level));
            }

            @Override
            public void sendDirectNotification(String registrationNumber, NotificationLevel level) {
                Endpoint.getEndpoint(arrowheadContext, SEND_NOTIFICATION_SERVICE_DEFINITION)
                        .consumeServiceHTTP(new SendNotificationRequestDto(registrationNumber, level));
            }
        };

        itsCentralLogic = new ItsCentralLogicImpl(vehicleCommunicator);
    }

    //-------------------------------------------------------------------------------------------------
    @GetMapping(path = CommonConstants.ECHO_URI)
    public String echo() {
        return "Got it!";
    }

    //-------------------------------------------------------------------------------------------------
    @PostMapping(path = ItsCentralProviderInternalConstants.REPORT_TRAIN_URI, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String reportTrain(@RequestBody final ReportTrainRequestDto dto) {
        logger.info(dto);
        if (dto.getSensor() == null || dto.getMessage() == null) {
            throw new BadPayloadException("sensor and message fields are mandatory");
        }
        itsCentralLogic.reportTrain(dto.getSensor(), dto.getMessage());

        return "OK";
    }

    //-------------------------------------------------------------------------------------------------
    @PostMapping(path = ItsCentralProviderInternalConstants.REPORT_VEHICLE_URI, consumes = MediaType.APPLICATION_JSON_VALUE)
    public boolean reportVehicle(@RequestBody final ReportVehicleRequestDto dto) {
        logger.info(dto);
        if (dto.getRegistrationNumber() == null || dto.getMessage() == null) {
            throw new BadPayloadException("registrationNumber and message fields are mandatory");
        }
        return itsCentralLogic.reportVehicle(dto.getRegistrationNumber(), dto.getMessage());
    }
}
