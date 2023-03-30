package hu.bme.mit.swsv.itssos.itscentral.logic.impl;

import hu.bme.mit.swsv.itssos.itscentral.logic.*;

import java.util.HashSet;
import java.util.Set;

import static hu.bme.mit.swsv.itssos.itscentral.logic.NotificationLevel.*;

// DO NOT ADD NEW PUBLIC OR PROTECTED MEMBERS TO THIS CLASS
public class ItsCentralLogicImpl implements ItsCentralLogic {
    private final VehicleCommunicator vehicleCommunicator;

    private final Set<String> registeredVehicles;

    // DO NOT MODIFY THE SIGNATURE OF THE CONSTRUCTOR
    public ItsCentralLogicImpl(VehicleCommunicator vehicleCommunicator) {
        this(vehicleCommunicator, new HashSet<>());
    }

    // DO NOT MODIFY THE SIGNATURE OF THE CONSTRUCTOR
    // separate constructor for testing
    protected ItsCentralLogicImpl(VehicleCommunicator vehicleCommunicator, Set<String> registeredVehicles) {
        this.vehicleCommunicator = vehicleCommunicator;
        this.registeredVehicles = registeredVehicles;
    }

    @Override
    public void reportTrain(SensorType sensorType, SensorMessageType messageType) {
        if (messageType != null && sensorType != null) {
            if (sensorType == SensorType.PROXIMITY) {
                switch (messageType) {
                    case REGISTER:
                        vehicleCommunicator.sendBroadcastNotification(PASS_SLOWLY);
                        break;
                    case ARRIVING:
                        vehicleCommunicator.sendBroadcastNotification(STOP);
                        break;
                    case LEFT:
                        vehicleCommunicator.sendBroadcastNotification(PASS_SLOWLY);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown SensorMessageType");
                }
            } else if (sensorType == SensorType.TIMETABLE) {
                switch (messageType) {
                    case REGISTER:
                    vehicleCommunicator.sendBroadcastNotification(PASS_SLOWLY);
                        break;
                    case LEFT:
                        vehicleCommunicator.sendBroadcastNotification(LOOK_AROUND);
                        break;
                    case ARRIVING:
                        vehicleCommunicator.sendBroadcastNotification(TRAIN_MIGHT_ARRIVE);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown SensorMessageType");
                }
            }
        }
    }

    @Override
    public boolean reportVehicle(String registrationNumber, VehicleMessageType message) {

        if (message != null && registrationNumber != null && !registrationNumber.equals("")) {
            switch (message) {
                case ARRIVING:
                    return registeredVehicles.add(registrationNumber);
                case LEFT:
                    return registeredVehicles.remove(registrationNumber);
                default:
                    throw new IllegalArgumentException("Unknown VehicleMessageType");
            }
        }

        return false;
    }
}
