// Copyright Mark Raymond Jr. 2022. All Rights Reserved
package fyi.meld.perimeter;

import java.util.HashMap;
import java.util.Map;

public final class Constants {

    public static final String PERIMETER_TAG = "Perimeter";
    public static final String ALL_ACTIVE_FENCES_EXTRA = "fencesActiveWhenAdded";
    public static final String FOREGROUND_ALIAS = "foreground";
    public static final String BACKGROUND_ALIAS = "background";
    public static final String PERMISSION_DENIED_NOTICE = "";
    public static final String CLIENT_INITIALIZED = "Geofencing client has been successfully initialized.";
    public static final String CLIENT_UNINITIALIZED = "";
    public static final int STANDARD_GEOFENCE_RESPONSIVENESS_MILLISECONDS = 120000;
    public static final int LOCATIONS_PERMISSIONS_REQUEST_CODE = 206314;
    public enum TRANSITION_TYPE {
        ENTER,
        EXIT,
        BOTH
    }

    public enum PERIMETER_ERROR {
        CLIENT_UNINITIALIZED,
        GENERIC_PLATFORM_ERROR,
        INCORRECT_PERMISSIONS,
        FOREGROUND_DENIED,
        INVALID_FENCE_OBJ,
        ALREADY_FENCED,
        NO_OR_INVALID_ARGS,
        FENCE_NOT_FOUND,
        ANDROID_FAILED_PARSING_INTENT_EXTRAS,
        ANDROID_METHOD_UNAVAILABLE_API_VER,
        ANDROID_FAILED_PACK_INTENT
    }

    public static final Map<PERIMETER_ERROR, String> ERROR_MESSAGES = new HashMap<PERIMETER_ERROR, String>() {{
        put(PERIMETER_ERROR.CLIENT_UNINITIALIZED, "Geofencing client has not been initialized, this should happen automatically after permissions are granted. Try requesting permissions again.");
        put(PERIMETER_ERROR.GENERIC_PLATFORM_ERROR, "A platform specific error has occurred.");
        put(PERIMETER_ERROR.INCORRECT_PERMISSIONS, "Perimeter does not have any of the required location permissions.");
        put(PERIMETER_ERROR.FOREGROUND_DENIED, "This method requires foreground permissions from ACCESS_COARSE_LOCATION permissions first. Also ensure you've called Perimeter.requestForegroundPermissions before calling this method.");
        put(PERIMETER_ERROR.INVALID_FENCE_OBJ, "Perimeter does not have any of the required location permissions.");
        put(PERIMETER_ERROR.ALREADY_FENCED, "A region with the specified UID or coordinates is already fenced.");
        put(PERIMETER_ERROR.NO_OR_INVALID_ARGS, "Invalid arguments for this function.");
        put(PERIMETER_ERROR.FENCE_NOT_FOUND, "A fence with that UID was not found in the list of active fences.");
        put(PERIMETER_ERROR.ANDROID_FAILED_PARSING_INTENT_EXTRAS, "Failed to parse intent extras while reconciling triggered and available.");
        put(PERIMETER_ERROR.ANDROID_METHOD_UNAVAILABLE_API_VER, "This method is only available on Android Q or later.");
        put(PERIMETER_ERROR.ANDROID_FAILED_PACK_INTENT, "Failed to pack intent data while attempting to create a new fence.");
    }};
}
