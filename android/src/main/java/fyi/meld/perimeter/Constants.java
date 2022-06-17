// Copyright Mark Raymond Jr. 2022. All Rights Reserved
package fyi.meld.perimeter;

public final class Constants {

    public static final String PERIMETER_TAG = "Perimeter";
    public static final String ALL_ACTIVE_FENCES_EXTRA = "fencesActiveWhenAdded";
    public static final String FOREGROUND_ALIAS = "foreground";
    public static final String BACKGROUND_ALIAS = "background";
    public static final String PERMISSION_DENIED_NOTICE = "Perimeter does not have any of the required location permissions.";
    public static final String CLIENT_INITIALIZED = "Geofencing client has been successfully initialized.";
    public static final String CLIENT_UNINITIALIZED = "Geofencing client has not been initialized, this should happen automatically after permissions are granted. Try requesting permissions again.";
    public static final int STANDARD_GEOFENCE_RESPONSIVENESS_MILLISECONDS = 120000;
    public static final int LOCATIONS_PERMISSIONS_REQUEST_CODE = 206314;
    public enum TRANSITION_TYPE {
        ENTER,
        EXIT,
        BOTH
    }
}
