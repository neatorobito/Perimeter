// Copyright Mark Raymond Jr. 2022. All Rights Reserved
package fyi.karm.perimeter;

import android.Manifest;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;

public final class Constants {

    public static final String PERIMETER_TAG = "Perimeter";
    public static final String ALL_ACTIVE_FENCES_EXTRA = "fencesActiveWhenAdded";
    public static final String FOREGROUND_ALIAS = "foreground";
    public static final String BACKGROUND_ALIAS = "background";
    public static final String CLIENT_INITIALIZED = "Geofencing client has been successfully initialized.";
    public static final int STANDARD_GEOFENCE_RESPONSIVENESS_MILLISECONDS = 60000;
    public static final int STANDARD_GEOFENCE_DWELL_DELAY_MILLISECONDS = 60000;
    public static final int ANDROID_FENCE_LIMIT = 100;
    public static final int LOCATIONS_PERMISSIONS_REQUEST_CODE = 206314;
    public static final int MIN_FENCE_RADIUS = 200;
    public static final int MAX_FENCE_RADIUS = 2000;

    public static final int MONITOR_ENTER = 8;
    public static final int MONITOR_EXIT = 9;
    public static final int MONITOR_BOTH = 10;

    public static String[] STANDARD_LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    public enum PERIMETER_ERROR {
        GEOFENCING_UNAVAILABLE,
        CLIENT_UNINITIALIZED,
        GENERIC_PLATFORM_ERROR,
        INCORRECT_PERMISSIONS,
        FOREGROUND_DENIED,
        INVALID_FENCE_OBJ,
        ALREADY_FENCED,
        NO_OR_INVALID_ARGS,
        FENCE_NOT_FOUND,
        TOO_MANY_FENCES
    }

    public enum ANDROID_PLATFORM_EVENT {
        FAILED_PARSING_INTENT_EXTRAS(200),
        METHOD_UNAVAILABLE_API_VER(201),
        FAILED_PACK_INTENT(202),
        FAILED_RESTORING_FENCES(203),
        FOREGROUND_WITH_EXISTING_FENCES(204);
        private final int id;
        ANDROID_PLATFORM_EVENT(int id) { this.id = id; }
        public int getValue() { return id; }
    }

    public static final Map<Integer, String> ERROR_MESSAGES = new HashMap<Integer, String>() {{
        put(PERIMETER_ERROR.GEOFENCING_UNAVAILABLE.ordinal(), "This device does not support geofencing.");
        put(PERIMETER_ERROR.CLIENT_UNINITIALIZED.ordinal(), "Geofencing client has not been initialized, this should happen automatically after permissions are granted. Try requesting permissions again.");
        put(PERIMETER_ERROR.GENERIC_PLATFORM_ERROR.ordinal(), "A platform specific error has occurred.");
        put(PERIMETER_ERROR.INCORRECT_PERMISSIONS.ordinal(), "Perimeter does not have any of the required location permissions.");
        put(PERIMETER_ERROR.FOREGROUND_DENIED.ordinal(), "This method requires foreground permissions from ACCESS_COARSE_LOCATION permissions first. Also ensure you've called Perimeter.requestForegroundPermissions before calling this method.");
        put(PERIMETER_ERROR.INVALID_FENCE_OBJ.ordinal(), "An invalid fence object was supplied.");
        put(PERIMETER_ERROR.ALREADY_FENCED.ordinal(), "A region with the specified UID or coordinates is already fenced.");
        put(PERIMETER_ERROR.NO_OR_INVALID_ARGS.ordinal(), "Invalid arguments for this function.");
        put(PERIMETER_ERROR.FENCE_NOT_FOUND.ordinal(), "A fence with that UID was not found in the list of active fences.");
        put(PERIMETER_ERROR.TOO_MANY_FENCES.ordinal(), "Cannot exceed Android platform limit of 100 fences. Please remove a region first and then try to add this one.");
        put(ANDROID_PLATFORM_EVENT.FAILED_PARSING_INTENT_EXTRAS.getValue(), "Failed to parse intent extras while reconciling triggered and available.");
        put(ANDROID_PLATFORM_EVENT.METHOD_UNAVAILABLE_API_VER.getValue(), "This method is only available on Android Q or later.");
        put(ANDROID_PLATFORM_EVENT.FAILED_PACK_INTENT.getValue(), "Failed to pack intent data while attempting to create a new fence.");
        put(ANDROID_PLATFORM_EVENT.FAILED_RESTORING_FENCES.getValue(), "An error occurred while attempting to restore fences from SharedPrefs.");
    }};
}
