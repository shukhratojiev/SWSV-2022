package hu.bme.mit.swsv.itssos.itscentral;

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.arrowhead.client.skeleton.provider.ItsCentralApplicationInitListener;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import hu.bme.mit.swsv.itssos.itscentral.logic.VehicleMessageType;
import hu.bme.mit.swsv.itssos.vehicle.SendNotificationRequestDto;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static eu.arrowhead.common.CommonConstants.ECHO_URI;
import static hu.bme.mit.swsv.itssos.itscentral.ItsCentralProviderInternalConstants.ITSCENTRAL_URI;
import static hu.bme.mit.swsv.itssos.itscentral.ItsCentralProviderInternalConstants.REPORT_TRAIN_FULL_URI;
import static hu.bme.mit.swsv.itssos.itscentral.ItsCentralProviderInternalConstants.REPORT_VEHICLE_FULL_URI;
import static hu.bme.mit.swsv.itssos.itscentral.logic.NotificationLevel.PASS_SLOWLY;
import static hu.bme.mit.swsv.itssos.itscentral.logic.NotificationLevel.STOP;
import static hu.bme.mit.swsv.itssos.itscentral.logic.NotificationLevel.TRAIN_MIGHT_ARRIVE;
import static hu.bme.mit.swsv.itssos.itscentral.logic.SensorMessageType.REGISTER;
import static hu.bme.mit.swsv.itssos.itscentral.logic.SensorMessageType.ARRIVING;
import static hu.bme.mit.swsv.itssos.itscentral.logic.SensorMessageType.LEFT;
import static hu.bme.mit.swsv.itssos.itscentral.logic.SensorType.PROXIMITY;
import static hu.bme.mit.swsv.itssos.itscentral.logic.SensorType.TIMETABLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;

import java.io.FileNotFoundException;
import java.io.FileReader;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ItsCentralIntegrationTest {

    // =================================================================================================
    // members
    private static final Logger logger = LogManager.getLogger(ItsCentralIntegrationTest.class);

    private static final String SEND_NOTIFICATION_URI = "/vehicle-communicator/send-notification";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    private ItsCentralApplicationInitListener initListener;

    @Autowired
    private ItsCentralController controller;

    @Rule
    public WireMockRule serviceRegistryMock = new WireMockRule(
            options().notifier(new ConsoleNotifier(true)).port(Defaults.DEFAULT_SERVICE_REGISTRY_PORT));
    @Rule
    public WireMockRule orchestratorMock = new WireMockRule(
            options().notifier(new ConsoleNotifier(true)).port(Defaults.DEFAULT_ORCHESTRATOR_PORT));
    @Rule
    public WireMockRule vehicleCommunicatorMock = new WireMockRule(
            options().notifier(new ConsoleNotifier(true)).port(8889));

    // =================================================================================================
    // methods

    @Before
    public void setup() {
        logger.info("setup: START");

        vehicleCommunicatorMock.stubFor(post(urlPathEqualTo(SEND_NOTIFICATION_URI))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("OK")));

        initListener.init(applicationContext);
        controller.init();

        logger.info("setup: END");
    }

    // -------------------------------------------------------------------------------------------------
    @Test
    public void testItsCentralEcho() {
        logger.info("testItsCentralEcho: START");
        // Arrange

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(ITSCENTRAL_URI + ECHO_URI, String.class);

        // Assert
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), containsString("Got it!"));
        logger.info("testItsCentralEcho: END");
    }

    @Test
    public void testProximityRegister() {
        logger.info("testProximityRegister: START");
        // Arrange
        ReportTrainRequestDto payload = new ReportTrainRequestDto(PROXIMITY, REGISTER);

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(REPORT_TRAIN_FULL_URI, payload, String.class);

        // Assert
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), containsString("OK"));

        String expectedNotification = Utilities.toJson(new SendNotificationRequestDto(null, PASS_SLOWLY));
        vehicleCommunicatorMock.verify(1, postRequestedFor(urlEqualTo(SEND_NOTIFICATION_URI))
                .withRequestBody(equalToJson(expectedNotification))
                .withHeader("Content-Type", equalTo("application/json")));
        logger.info("testProximityRegister: END");
    }

    @Test
    public void testMultipleVehiclesInIntersection() throws FileNotFoundException, ParseException {

        logger.info("testMultipleVehiclesInIntersection: START");
        // Arrange
        ReportVehicleRequestDto payloadVehicle1 = new ReportVehicleRequestDto("1", VehicleMessageType.ARRIVING);
        ReportVehicleRequestDto payloadVehicle2 = new ReportVehicleRequestDto("1", VehicleMessageType.ARRIVING);
        ReportVehicleRequestDto payloadVehicle3 = new ReportVehicleRequestDto("3", VehicleMessageType.ARRIVING);
        ReportVehicleRequestDto payloadVehicle4 = new ReportVehicleRequestDto("4", VehicleMessageType.ARRIVING);

        // Act
        ResponseEntity<String> responseVehicle1 = restTemplate.postForEntity(REPORT_VEHICLE_FULL_URI, payloadVehicle1, String.class);
        ResponseEntity<String> responseVehicle2 = restTemplate.postForEntity(REPORT_VEHICLE_FULL_URI, payloadVehicle2, String.class);
        ResponseEntity<String> responseVehicle3 = restTemplate.postForEntity(REPORT_VEHICLE_FULL_URI, payloadVehicle3, String.class);
        ResponseEntity<String> responseVehicle4 = restTemplate.postForEntity(REPORT_VEHICLE_FULL_URI, payloadVehicle4, String.class);


        // Assert
        assertThat(responseVehicle1.getStatusCodeValue(), is(200));
        assertThat(responseVehicle1.getBody(), containsString("true"));

        assertThat(responseVehicle2.getStatusCodeValue(), is(200));
        assertThat(responseVehicle2.getBody(), containsString("false"));

        assertThat(responseVehicle3.getStatusCodeValue(), is(200));
        assertThat(responseVehicle3.getBody(), containsString("true"));

        assertThat(responseVehicle4.getStatusCodeValue(), is(200));
        assertThat(responseVehicle4.getBody(), containsString("true"));

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader("src/test/resources/__files/serviceregistry-register-request-report-vehicle.json"));
        serviceRegistryMock.verify(1, postRequestedFor(urlEqualTo("/serviceregistry/register"))
                .withRequestBody(equalToJson(Utilities.toJson(obj))));

        logger.info("SendStopToCarAfterDetect: END");
    }
    
    @Test
    public void testDirectNotificationsToMultipleVechicles() throws FileNotFoundException, ParseException {
        logger.info("testDirectNotificationsToMultipleVechicles: START");
        // Arrange
        ReportTrainRequestDto payloadRegisterTimetable = new ReportTrainRequestDto(PROXIMITY, REGISTER);

        // Act
        ResponseEntity<String> responseTrainRegister = restTemplate.postForEntity(REPORT_TRAIN_FULL_URI,
                payloadRegisterTimetable, String.class);

        // Assert
        assertThat(responseTrainRegister.getStatusCodeValue(), is(200));
        assertThat(responseTrainRegister.getBody(), containsString("OK"));

        ReportVehicleRequestDto payloadVehicle1 = new ReportVehicleRequestDto("1", VehicleMessageType.ARRIVING);
        ReportVehicleRequestDto payloadVehicle2 = new ReportVehicleRequestDto("2", VehicleMessageType.ARRIVING);
        ReportVehicleRequestDto payloadVehicle3 = new ReportVehicleRequestDto("3", VehicleMessageType.ARRIVING);
        ReportVehicleRequestDto payloadVehicle4 = new ReportVehicleRequestDto("4", VehicleMessageType.ARRIVING);

        // Act
        ResponseEntity<String> responseVehicle1 = restTemplate.postForEntity(REPORT_VEHICLE_FULL_URI, payloadVehicle1, String.class);
        ResponseEntity<String> responseVehicle2 = restTemplate.postForEntity(REPORT_VEHICLE_FULL_URI, payloadVehicle2, String.class);
        ResponseEntity<String> responseVehicle3 = restTemplate.postForEntity(REPORT_VEHICLE_FULL_URI, payloadVehicle3, String.class);
        ResponseEntity<String> responseVehicle4 = restTemplate.postForEntity(REPORT_VEHICLE_FULL_URI, payloadVehicle4, String.class);

        // Assert
        assertThat(responseVehicle1.getStatusCodeValue(), is(200));
        assertThat(responseVehicle1.getBody(), containsString("true"));

        assertThat(responseVehicle2.getStatusCodeValue(), is(200));
        assertThat(responseVehicle2.getBody(), containsString("true"));

        assertThat(responseVehicle3.getStatusCodeValue(), is(200));
        assertThat(responseVehicle3.getBody(), containsString("true"));

        assertThat(responseVehicle4.getStatusCodeValue(), is(200));
        assertThat(responseVehicle4.getBody(), containsString("true"));

        ReportTrainRequestDto trainArriving = new ReportTrainRequestDto(PROXIMITY, ARRIVING);
        ResponseEntity<String> trainArrivingResponse = restTemplate.postForEntity(REPORT_TRAIN_FULL_URI, trainArriving, String.class);

        assertThat(trainArrivingResponse.getStatusCodeValue(), is(200));
        assertThat(trainArrivingResponse.getBody(), containsString("OK"));

        String expectedNotification = Utilities.toJson(new SendNotificationRequestDto(null, STOP));

        vehicleCommunicatorMock.verify(1,
                postRequestedFor(urlEqualTo(SEND_NOTIFICATION_URI))
                        .withRequestBody(equalToJson(expectedNotification))
                        .withHeader("Content-Type", equalTo("application/json")));

        logger.info("testDirectNotificationsToMultipleVechicles: END");
    }

    @Test
    public void testSendStopTrainArriving() {
            logger.info("testSendStopTrainArriving: START");

            // Arrange
            ReportTrainRequestDto payloadRegister = new ReportTrainRequestDto(PROXIMITY, REGISTER);

            // Act
            ResponseEntity<String> responseRegister = restTemplate.postForEntity(REPORT_TRAIN_FULL_URI,
                            payloadRegister, String.class);

            // Assert
            assertThat(responseRegister.getStatusCodeValue(), is(200));
            assertThat(responseRegister.getBody(), containsString("OK"));

            // Arrange
            ReportVehicleRequestDto payloadVehicle1 = new ReportVehicleRequestDto("1", VehicleMessageType.ARRIVING);

            // Act
            ResponseEntity<String> responseVehicle1 = restTemplate.postForEntity(REPORT_VEHICLE_FULL_URI,
                            payloadVehicle1, String.class);

            // Assert
            assertThat(responseVehicle1.getStatusCodeValue(), is(200));
            assertThat(responseVehicle1.getBody(), containsString("true"));

            // Arrange
            ReportTrainRequestDto payload = new ReportTrainRequestDto(PROXIMITY, ARRIVING);

            // Act
            ResponseEntity<String> response = restTemplate.postForEntity(REPORT_TRAIN_FULL_URI, payload,
                            String.class);

            // Assert
            assertThat(response.getStatusCodeValue(), is(200));
            assertThat(response.getBody(), containsString("OK"));

            String expectedNotification = Utilities.toJson(new SendNotificationRequestDto(null, STOP));

            vehicleCommunicatorMock.verify(1,
                            postRequestedFor(urlEqualTo(SEND_NOTIFICATION_URI))
                                            .withRequestBody(equalToJson(expectedNotification))
                                            .withHeader("Content-Type", equalTo("application/json")));

            logger.info("testSendStopTrainArriving: END");
    } 

    @Test
    public void SendTrainMightArriveToCarAfterDetect() throws FileNotFoundException, ParseException {

        logger.info("SendTrainMightArriveToCarAfterDetect: START");
        // Arrange

        ReportVehicleRequestDto payloadVehicle = new ReportVehicleRequestDto("1", VehicleMessageType.ARRIVING);
        ReportTrainRequestDto payloadTrain = new ReportTrainRequestDto(TIMETABLE, ARRIVING);

        // Act
        ResponseEntity<String> responseVehicle = restTemplate.postForEntity(REPORT_VEHICLE_FULL_URI, payloadVehicle, String.class);
        ResponseEntity<String> responseTrain = restTemplate.postForEntity(REPORT_TRAIN_FULL_URI, payloadTrain, String.class);


        // Assert
        assertThat(responseVehicle.getStatusCodeValue(), is(200));
        assertThat(responseVehicle.getBody(), containsString("true"));

        assertThat(responseTrain.getStatusCodeValue(), is(200));
        assertThat(responseTrain.getBody(), containsString("OK"));


        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(new FileReader("src/test/resources/__files/serviceregistry-register-request-report-vehicle.json"));
        } catch (net.minidev.json.parser.ParseException e) {
            e.printStackTrace();
        }

        String expectedNotification = Utilities.toJson(new SendNotificationRequestDto(null,TRAIN_MIGHT_ARRIVE));

        serviceRegistryMock.verify(1, postRequestedFor(urlEqualTo("/serviceregistry/register"))
                .withRequestBody(equalToJson(Utilities.toJson(obj))));

        vehicleCommunicatorMock.verify(1, postRequestedFor(urlEqualTo(SEND_NOTIFICATION_URI))
                .withRequestBody(equalToJson(expectedNotification))
                .withHeader("Content-Type", equalTo("application/json")));



        logger.info("SendTrainMightArriveToCarAfterDetect: END");
    }

    @Test
    public void UpdateNotificationLevel() throws FileNotFoundException, ParseException {

        logger.info("SendDirectNotification: START");
        // Arrange

        ReportVehicleRequestDto payloadVehicle = new ReportVehicleRequestDto("1", VehicleMessageType.ARRIVING);
        ReportTrainRequestDto payloadTrain = new ReportTrainRequestDto(PROXIMITY, ARRIVING);
        ReportTrainRequestDto payloadTrain2 = new ReportTrainRequestDto(TIMETABLE, ARRIVING);
        ReportTrainRequestDto payloadTrain3 = new ReportTrainRequestDto(PROXIMITY, LEFT);

        // Act
        ResponseEntity<String> responseVehicle = restTemplate.postForEntity(REPORT_VEHICLE_FULL_URI, payloadVehicle, String.class);
        ResponseEntity<String> responseTrain = restTemplate.postForEntity(REPORT_TRAIN_FULL_URI, payloadTrain, String.class);
        ResponseEntity<String> responseTrain2 = restTemplate.postForEntity(REPORT_TRAIN_FULL_URI, payloadTrain2, String.class);
        ResponseEntity<String> responseTrain3 = restTemplate.postForEntity(REPORT_TRAIN_FULL_URI, payloadTrain3, String.class);



        // Assert
        assertThat(responseVehicle.getStatusCodeValue(), is(200));
        assertThat(responseVehicle.getBody(), containsString("true"));
        assertThat(responseTrain.getStatusCodeValue(), is(200));
        assertThat(responseTrain.getBody(), containsString("OK"));
        assertThat(responseTrain2.getStatusCodeValue(), is(200));
        assertThat(responseTrain2.getBody(), containsString("OK"));
        assertThat(responseTrain3.getStatusCodeValue(), is(200));
        assertThat(responseTrain3.getBody(), containsString("OK"));

        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(new FileReader("src/test/resources/__files/serviceregistry-register-request-report-vehicle.json"));
        } catch (net.minidev.json.parser.ParseException e) {
            e.printStackTrace();
        }

        String expectedNotification = Utilities.toJson(new SendNotificationRequestDto(null,TRAIN_MIGHT_ARRIVE));
        String expectedNotification2 = Utilities.toJson(new SendNotificationRequestDto(null,STOP));
        String expectedNotification3= Utilities.toJson(new SendNotificationRequestDto(null,PASS_SLOWLY));
        serviceRegistryMock.verify(1, postRequestedFor(urlEqualTo("/serviceregistry/register"))
                .withRequestBody(equalToJson(Utilities.toJson(obj))));

        vehicleCommunicatorMock.verify(1, postRequestedFor(urlEqualTo(SEND_NOTIFICATION_URI))
                .withRequestBody(equalToJson(expectedNotification))
                .withHeader("Content-Type", equalTo("application/json")));
        vehicleCommunicatorMock.verify(1, postRequestedFor(urlEqualTo(SEND_NOTIFICATION_URI))
                .withRequestBody(equalToJson(expectedNotification2))
                .withHeader("Content-Type", equalTo("application/json")));
        vehicleCommunicatorMock.verify(1, postRequestedFor(urlEqualTo(SEND_NOTIFICATION_URI))
                .withRequestBody(equalToJson(expectedNotification3))
                .withHeader("Content-Type", equalTo("application/json")));



        logger.info("SendDirectNotification: END");

    }
    
    @Test
    public void IgnoreRegisterIfAlreadyRegistered() throws FileNotFoundException, ParseException {

        logger.info("IgnoreRegisterIfAlreadyRegistered: START");
        // Arrange

        ReportVehicleRequestDto payloadVehicle = new ReportVehicleRequestDto("1", VehicleMessageType.ARRIVING);
        ReportTrainRequestDto payloadTrain = new ReportTrainRequestDto(PROXIMITY, REGISTER);
        ReportTrainRequestDto payloadTrain1 = new ReportTrainRequestDto(PROXIMITY, REGISTER);

        // Act
        ResponseEntity<String> responseVehicle = restTemplate.postForEntity(REPORT_VEHICLE_FULL_URI, payloadVehicle, String.class);
        ResponseEntity<String> responseTrain = restTemplate.postForEntity(REPORT_TRAIN_FULL_URI, payloadTrain, String.class);
        // Assert
        assertThat(responseVehicle.getStatusCodeValue(), is(200));
        assertThat(responseVehicle.getBody(), containsString("true"));

        assertThat(responseTrain.getStatusCodeValue(), is(200));
        assertThat(responseTrain.getBody(), containsString("OK"));

        assertThat(responseTrain.getStatusCodeValue(), is(200));
        assertThat(responseTrain.getBody(), containsString("OK"));//Sensor already registered

        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(new FileReader("src/test/resources/__files/serviceregistry-register-request-report-vehicle.json"));
        } catch (net.minidev.json.parser.ParseException e) {
            e.printStackTrace();
        }

        String expectedNotification = Utilities.toJson(new SendNotificationRequestDto(null,TRAIN_MIGHT_ARRIVE));

        serviceRegistryMock.verify(1, postRequestedFor(urlEqualTo("/serviceregistry/register"))
                .withRequestBody(equalToJson(Utilities.toJson(obj))));

        logger.info("IgnoreRegisterIfAlreadyRegistered: END");

    }
  
    @Test
    public void IgnoreNotificationIfNotChangeTheLevel() throws FileNotFoundException, ParseException {

        logger.info("IgnoreNotificationIfNotChangeTheLevel: START");
        // Arrange

        ReportVehicleRequestDto payloadVehicle = new ReportVehicleRequestDto("1", VehicleMessageType.ARRIVING);
        ReportTrainRequestDto payloadTrain = new ReportTrainRequestDto(PROXIMITY, ARRIVING);
        ReportTrainRequestDto payloadTrain2 = new ReportTrainRequestDto(PROXIMITY, ARRIVING);
        ReportTrainRequestDto payloadTrain3 = new ReportTrainRequestDto(PROXIMITY, LEFT);
        ReportTrainRequestDto payloadTrain4 = new ReportTrainRequestDto(PROXIMITY, LEFT);
        ReportTrainRequestDto payloadTrain5 = new ReportTrainRequestDto(TIMETABLE, ARRIVING);


        // Act
        ResponseEntity<String> responseVehicle = restTemplate.postForEntity(REPORT_VEHICLE_FULL_URI, payloadVehicle, String.class);
        ResponseEntity<String> responseTrain = restTemplate.postForEntity(REPORT_TRAIN_FULL_URI, payloadTrain, String.class);
        ResponseEntity<String> responseTrain2 = restTemplate.postForEntity(REPORT_TRAIN_FULL_URI, payloadTrain2, String.class);
        ResponseEntity<String> responseTrain3 = restTemplate.postForEntity(REPORT_TRAIN_FULL_URI, payloadTrain3, String.class);
        ResponseEntity<String> responseTrain4 = restTemplate.postForEntity(REPORT_TRAIN_FULL_URI, payloadTrain4, String.class);
        ResponseEntity<String> responseTrain5 = restTemplate.postForEntity(REPORT_TRAIN_FULL_URI, payloadTrain5, String.class);



        // Assert
        assertThat(responseVehicle.getStatusCodeValue(), is(200));
        assertThat(responseVehicle.getBody(), containsString("true"));
        assertThat(responseTrain.getStatusCodeValue(), is(200));
        assertThat(responseTrain.getBody(), containsString("OK"));
        assertThat(responseTrain2.getStatusCodeValue(), is(200));
        assertThat(responseTrain2.getBody(), containsString("OK"));
        assertThat(responseTrain3.getStatusCodeValue(), is(200));
        assertThat(responseTrain3.getBody(), containsString("OK"));
        assertThat(responseTrain4.getStatusCodeValue(), is(200));
        assertThat(responseTrain4.getBody(), containsString("OK"));
        assertThat(responseTrain5.getStatusCodeValue(), is(200));
        assertThat(responseTrain5.getBody(), containsString("OK"));

        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(new FileReader("src/test/resources/__files/serviceregistry-register-request-report-vehicle.json"));
        } catch (net.minidev.json.parser.ParseException e) {
            e.printStackTrace();
        }

        String expectedNotification = Utilities.toJson(new SendNotificationRequestDto(null,TRAIN_MIGHT_ARRIVE));
        String expectedNotification2 = Utilities.toJson(new SendNotificationRequestDto(null,STOP));
        String expectedNotification3= Utilities.toJson(new SendNotificationRequestDto(null,PASS_SLOWLY));
        serviceRegistryMock.verify(1, postRequestedFor(urlEqualTo("/serviceregistry/register"))
                .withRequestBody(equalToJson(Utilities.toJson(obj))));

        vehicleCommunicatorMock.verify(1, postRequestedFor(urlEqualTo(SEND_NOTIFICATION_URI))
                .withRequestBody(equalToJson(expectedNotification))
                .withHeader("Content-Type", equalTo("application/json")));
        vehicleCommunicatorMock.verify(2, postRequestedFor(urlEqualTo(SEND_NOTIFICATION_URI))
                .withRequestBody(equalToJson(expectedNotification2))
                .withHeader("Content-Type", equalTo("application/json")));
        vehicleCommunicatorMock.verify(2, postRequestedFor(urlEqualTo(SEND_NOTIFICATION_URI))
                .withRequestBody(equalToJson(expectedNotification3))
                .withHeader("Content-Type", equalTo("application/json")));
        
        logger.info("IgnoreNotificationIfNotChangeTheLevel: END");

    }

 }
