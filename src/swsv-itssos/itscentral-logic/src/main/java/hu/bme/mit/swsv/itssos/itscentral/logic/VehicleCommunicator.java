package hu.bme.mit.swsv.itssos.itscentral.logic;

// DO NOT MODIFY THIS FILE
public interface VehicleCommunicator {
    void sendBroadcastNotification(NotificationLevel level);

    void sendDirectNotification(String registrationNumber, NotificationLevel level);
}
