package hu.bme.mit.swsv.itssos.itscentral.logic.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.java.annotation.GraphWalker;
import hu.bme.mit.swsv.itssos.itscentral.logic.VehicleCommunicator;
import hu.bme.mit.swsv.itssos.itscentral.logic.ItsCentralLogic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import hu.bme.mit.swsv.itssos.itscentral.logic.NotificationLevel;
import hu.bme.mit.swsv.itssos.itscentral.logic.SensorMessageType;
import hu.bme.mit.swsv.itssos.itscentral.logic.SensorType;
import org.graphwalker.core.generator.RandomPath;


@GraphWalker(start = "v_init", pathGenerator = RandomPath.class, stopCondition = EdgeCoverage.class)
public class ItsCentralModelTest extends ExecutionContext implements ItsCentralModel {

    private static final Logger logger = LogManager.getLogger(ItsCentralModelTest.class);
    private VehicleCommunicator vehicleCommunicator;
    private ItsCentralLogic itsCentralLogic;
    private static int countTrainMightArrive = 0;
    private static int countPassSlowly = 0;
    private static int countLookAround = 0;
    private static int countStop = 0;

    @Override
    public void v_initialized() {
        logger.debug("initialized");
    }

    @Override
    public void e_return() {
        logger.debug("returning");
    }

    @Override
    public void v_timetableLeft() {
        verify(vehicleCommunicator, times(countLookAround)).sendBroadcastNotification(NotificationLevel.LOOK_AROUND);
        
    }

    @Override
    public void e_timetableLeft() {
        itsCentralLogic.reportTrain(SensorType.TIMETABLE, SensorMessageType.LEFT);
        countLookAround++;
        
    }

    @Override
    public void v_proximityLeft() {
        verify(vehicleCommunicator, times(countPassSlowly)).sendBroadcastNotification(NotificationLevel.PASS_SLOWLY);
        
    }

    @Override
    public void v_proximityRegister() {
        verify(vehicleCommunicator, times(countPassSlowly)).sendBroadcastNotification(NotificationLevel.PASS_SLOWLY);
        
    }

    @Override
    public void v_timetableRegister() {
        verify(vehicleCommunicator, times(countPassSlowly)).sendBroadcastNotification(NotificationLevel.PASS_SLOWLY);
        
    }

    @Override
    public void e_timetableRegister() {
        itsCentralLogic.reportTrain(SensorType.TIMETABLE, SensorMessageType.REGISTER);
        countPassSlowly++;
        
    }

    @Override
    public void e_init() {
        vehicleCommunicator = mock(VehicleCommunicator.class);
        itsCentralLogic = new ItsCentralLogicImpl(vehicleCommunicator);
        
    }

    @Override
    public void e_proximityLeft() {
        itsCentralLogic.reportTrain(SensorType.PROXIMITY, SensorMessageType.LEFT);
        countPassSlowly++;
        
    }

    @Override
    public void e_proximityRegister() {
        itsCentralLogic.reportTrain(SensorType.PROXIMITY, SensorMessageType.REGISTER);
        countPassSlowly++;
        
    }

    @Override
    public void v_proximityArriving() {
        verify(vehicleCommunicator, times(countStop)).sendBroadcastNotification(NotificationLevel.STOP);
        
    }

    @Override
    public void v_timetableArriving() {
        verify(vehicleCommunicator, times(countTrainMightArrive))
                .sendBroadcastNotification(NotificationLevel.TRAIN_MIGHT_ARRIVE);
        
    }

    @Override
    public void e_proximityArriving() {
        itsCentralLogic.reportTrain(SensorType.PROXIMITY, SensorMessageType.ARRIVING);
        countStop++;
        
    }

    @Override
    public void v_init() {
        logger.debug("init");
        
    }

    @Override
    public void e_timetableArriving() {
        itsCentralLogic.reportTrain(SensorType.TIMETABLE, SensorMessageType.ARRIVING);
        countTrainMightArrive++;
        
    }




}
