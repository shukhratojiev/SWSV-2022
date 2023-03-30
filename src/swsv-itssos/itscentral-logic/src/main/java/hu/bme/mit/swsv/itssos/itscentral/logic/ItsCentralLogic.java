package hu.bme.mit.swsv.itssos.itscentral.logic;

// DO NOT MODIFY THIS FILE
public interface ItsCentralLogic {
    void reportTrain(SensorType sensorType, SensorMessageType message);

    boolean reportVehicle(String registrationNumber, VehicleMessageType message);
}
