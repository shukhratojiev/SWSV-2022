package hu.bme.mit.swsv.itssos.itscentral.logic.impl;

import hu.bme.mit.swsv.itssos.itscentral.logic.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.mock;

class ItsCentralTest {
    // DO NOT CHANGE THE TYPE OF THE FIELDS BELOW
    private VehicleCommunicator vehicleCommunicator;
    private ItsCentralLogic itsCentralLogic;

    @BeforeEach
    public void init() {
        vehicleCommunicator = mock(VehicleCommunicator.class);
        itsCentralLogic = new ItsCentralLogicImpl(vehicleCommunicator);
    }

    @Test
    void testReportVehicleArriving() {
        assertEquals("Should be true", true, itsCentralLogic.reportVehicle("52528", VehicleMessageType.ARRIVING));
    }

    @Test
    void testReportVehicleWithTheSameNumber() {
        assertEquals("Should be true", true, itsCentralLogic.reportVehicle("1234", VehicleMessageType.ARRIVING));
        assertEquals("Should be false", false, itsCentralLogic.reportVehicle("1234", VehicleMessageType.ARRIVING));
    }

    @Test
    void testReportVehicleRemovingAlreadyExistedLeft() {
        assertEquals("Should be true", true, itsCentralLogic.reportVehicle("6588", VehicleMessageType.ARRIVING));
        assertEquals("Should be true", true, itsCentralLogic.reportVehicle("6588", VehicleMessageType.LEFT));
    }

    @Test
    void testReportVehicleRemovingNonStoredVeichle() {
        assertEquals("Should be false", false, itsCentralLogic.reportVehicle("9999", VehicleMessageType.LEFT));
    }

    @Test
    void testReportVehicleNullValueForVehicleMessageType() {
        assertEquals("Should be false", false, itsCentralLogic.reportVehicle("74556", null));
    }

    @Test
    void testReportVehicleNullValueForRegistrationNumber() {
        assertEquals("Should be false", false, itsCentralLogic.reportVehicle(null, VehicleMessageType.ARRIVING));
    }

    @Test
    void testReportVehicleEmptyValueForRegistrationNumber() {
        assertEquals("Should be false", false, itsCentralLogic.reportVehicle("", VehicleMessageType.ARRIVING));
    }

    @Test
    void testReportVehicleThrowException() {
        assertThrows(IllegalArgumentException.class, () ->  {
            itsCentralLogic.reportVehicle("125366", VehicleMessageType.valueOf("ON_HOLD"));
        });

    }


    //  Tests for report train

    @Test
    void testReportTrainProximityArriving() {
        itsCentralLogic.reportTrain(SensorType.PROXIMITY, SensorMessageType.ARRIVING);
        verify(vehicleCommunicator,times(1)).sendBroadcastNotification(NotificationLevel.STOP);
        verify(vehicleCommunicator,times(0)).sendBroadcastNotification(NotificationLevel.LOOK_AROUND);
        verify(vehicleCommunicator,times(0)).sendBroadcastNotification(NotificationLevel.PASS_SLOWLY);
        verify(vehicleCommunicator,times(0)).sendBroadcastNotification(NotificationLevel.TRAIN_MIGHT_ARRIVE);
    }

    @Test
    void testReportTrainProximityRegister() {
        itsCentralLogic.reportTrain(SensorType.PROXIMITY, SensorMessageType.REGISTER);
        verify(vehicleCommunicator,times(1)).sendBroadcastNotification(NotificationLevel.PASS_SLOWLY);
    }

    @Test
    void testReportTrainProximityLeft() {
        itsCentralLogic.reportTrain(SensorType.PROXIMITY, SensorMessageType.LEFT);
        verify(vehicleCommunicator,times(1)).sendBroadcastNotification(NotificationLevel.PASS_SLOWLY);
    }

    @Test
    void testReportTrainProximityNullValueForSensorMessageType() {
        itsCentralLogic.reportTrain(SensorType.PROXIMITY, null);
        verify(vehicleCommunicator,times(0)).sendBroadcastNotification(NotificationLevel.PASS_SLOWLY);
        verify(vehicleCommunicator,times(0)).sendBroadcastNotification(NotificationLevel.STOP);
        verify(vehicleCommunicator,times(0)).sendBroadcastNotification(NotificationLevel.TRAIN_MIGHT_ARRIVE);
        verify(vehicleCommunicator,times(0)).sendBroadcastNotification(NotificationLevel.LOOK_AROUND);
    }

    @Test
    void testReportTrainProximityNullValueForSensorType() {
        itsCentralLogic.reportTrain(null, SensorMessageType.LEFT);
        verify(vehicleCommunicator,times(0)).sendBroadcastNotification(NotificationLevel.PASS_SLOWLY);
        verify(vehicleCommunicator,times(0)).sendBroadcastNotification(NotificationLevel.STOP);
        verify(vehicleCommunicator,times(0)).sendBroadcastNotification(NotificationLevel.TRAIN_MIGHT_ARRIVE);
        verify(vehicleCommunicator,times(0)).sendBroadcastNotification(NotificationLevel.LOOK_AROUND);
    }


    @Test
    void testReportTrainProximityThrowException() {
        assertThrows(IllegalArgumentException.class, () ->  {
            itsCentralLogic.reportTrain(SensorType.PROXIMITY, SensorMessageType.valueOf("STOPED"));
        });

    }
}
