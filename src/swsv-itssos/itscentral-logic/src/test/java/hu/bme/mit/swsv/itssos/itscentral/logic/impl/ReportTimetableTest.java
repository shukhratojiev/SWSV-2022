package hu.bme.mit.swsv.itssos.itscentral.logic.impl;

import hu.bme.mit.swsv.itssos.itscentral.logic.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

class ReportTimetableTest {
    private VehicleCommunicator vehicleCommunicator;
    private ItsCentralLogic itsCentralLogic;

    @BeforeEach
    public void init() {
        vehicleCommunicator = mock(VehicleCommunicator.class);
        itsCentralLogic = new ItsCentralLogicImpl(vehicleCommunicator);
    }

    @Test
    void testReportTimetableTrainRegister() {
        itsCentralLogic.reportTrain(SensorType.TIMETABLE, SensorMessageType.REGISTER);
        verify(vehicleCommunicator,times(1)).sendBroadcastNotification(NotificationLevel.PASS_SLOWLY);
        verify(vehicleCommunicator,times(0)).sendBroadcastNotification(NotificationLevel.LOOK_AROUND);
        verify(vehicleCommunicator,times(0)).sendBroadcastNotification(NotificationLevel.TRAIN_MIGHT_ARRIVE);
    }

    @Test
    void testReportTimetableTrainLeft() {
        itsCentralLogic.reportTrain(SensorType.TIMETABLE, SensorMessageType.LEFT);
        verify(vehicleCommunicator,times(1)).sendBroadcastNotification(NotificationLevel.LOOK_AROUND);
        verify(vehicleCommunicator,times(0)).sendBroadcastNotification(NotificationLevel.TRAIN_MIGHT_ARRIVE);
        verify(vehicleCommunicator,times(0)).sendBroadcastNotification(NotificationLevel.PASS_SLOWLY);
    }

    @Test
    void testReportTimetableTrainArriving() {
        itsCentralLogic.reportTrain(SensorType.TIMETABLE, SensorMessageType.ARRIVING);
        verify(vehicleCommunicator,times(1)).sendBroadcastNotification(NotificationLevel.TRAIN_MIGHT_ARRIVE);
        verify(vehicleCommunicator,times(0)).sendBroadcastNotification(NotificationLevel.LOOK_AROUND);
        verify(vehicleCommunicator,times(0)).sendBroadcastNotification(NotificationLevel.PASS_SLOWLY);
    }

    @Test
    void testReportTimetableThrowException() {
        assertThrows(IllegalArgumentException.class, () ->  {
            itsCentralLogic.reportTrain(SensorType.TIMETABLE, SensorMessageType.valueOf("TEST"));
        });
    }
}