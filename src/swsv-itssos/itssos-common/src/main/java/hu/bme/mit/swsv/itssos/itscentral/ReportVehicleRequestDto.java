package hu.bme.mit.swsv.itssos.itscentral;

import hu.bme.mit.swsv.itssos.itscentral.logic.VehicleMessageType;

import java.io.Serializable;

public class ReportVehicleRequestDto implements Serializable {

    //=================================================================================================
    // members
    private static final long serialVersionUID = -1926754623441738922L;

    private String registrationNumber;
    private VehicleMessageType message;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ReportVehicleRequestDto(String registrationNumber, VehicleMessageType message) {
        this.registrationNumber = registrationNumber;
        this.message = message;
    }

    //-------------------------------------------------------------------------------------------------
    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public VehicleMessageType getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ReportVehicleRequestDto{" +
                "registrationNumber='" + registrationNumber + '\'' +
                ", message=" + message +
                '}';
    }
}
