package hu.bme.mit.swsv.itssos.itscentral;

public class ItsCentralProviderInternalConstants {

    //=================================================================================================
    // members

    public static final String BASE_PACKAGE = "hu.bme.mit.swsv.itssos";

    public static final String ITSCENTRAL_URI = "/itscentral";
    public static final String REPORT_TRAIN_URI = '/' + ItsCentralProviderConstants.REPORT_TRAIN_SERVICE_DEFINITION;
    public static final String REPORT_TRAIN_FULL_URI = ITSCENTRAL_URI + REPORT_TRAIN_URI;
    public static final String REPORT_VEHICLE_URI = '/' + ItsCentralProviderConstants.REPORT_VEHICLE_SERVICE_DEFINITION;
    public static final String REPORT_VEHICLE_FULL_URI = ITSCENTRAL_URI + REPORT_VEHICLE_URI;

    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    private ItsCentralProviderInternalConstants() {
        throw new UnsupportedOperationException();
    }
}
