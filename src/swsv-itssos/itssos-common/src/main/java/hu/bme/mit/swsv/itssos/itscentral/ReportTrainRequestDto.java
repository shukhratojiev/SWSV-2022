package hu.bme.mit.swsv.itssos.itscentral;

import hu.bme.mit.swsv.itssos.itscentral.logic.SensorMessageType;
import hu.bme.mit.swsv.itssos.itscentral.logic.SensorType;

import java.io.Serializable;

public class ReportTrainRequestDto implements Serializable {

    //=================================================================================================
    // members
    private static final long serialVersionUID = 6120482912813843842L;

    private SensorType sensor;
    private SensorMessageType message;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ReportTrainRequestDto(SensorType sensor, SensorMessageType message) {
        this.sensor = sensor;
        this.message = message;
    }

    //-------------------------------------------------------------------------------------------------
    public SensorType getSensor() {
        return sensor;
    }

    public SensorMessageType getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ReportTrainRequestDto{" +
                "sensor=" + sensor +
                ", message=" + message +
                '}';
    }
}
