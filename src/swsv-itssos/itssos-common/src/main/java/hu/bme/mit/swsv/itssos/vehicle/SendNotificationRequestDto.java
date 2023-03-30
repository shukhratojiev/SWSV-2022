package hu.bme.mit.swsv.itssos.vehicle;

import hu.bme.mit.swsv.itssos.itscentral.logic.NotificationLevel;

import java.io.Serializable;

public class SendNotificationRequestDto implements Serializable {

    //=================================================================================================
    // members
    private static final long serialVersionUID = -6665231815463391411L;

    /**
     * registrationNumber = null means a broadcast message
     */
    private String registrationNumber;
    private NotificationLevel level;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------

    /**
     * @param registrationNumber null means a broadcast message
     */
    public SendNotificationRequestDto(String registrationNumber, NotificationLevel level) {
        this.registrationNumber = registrationNumber;
        this.level = level;
    }

    //-------------------------------------------------------------------------------------------------

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public NotificationLevel getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "SendNotificationRequestDto{" +
                "registrationNumber='" + registrationNumber + '\'' +
                ", level=" + level +
                '}';
    }
}
