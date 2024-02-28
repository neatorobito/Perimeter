// Copyright Mark Raymond Jr. 2022. All Rights Reserved

package fyi.karm.perimeter;

import static fyi.karm.perimeter.Constants.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.google.android.gms.common.GoogleApiAvailabilityLight;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class PerimeterReceiver extends BroadcastReceiver {

    public abstract void onFenceTriggered(Context context, ArrayList<JSObject> triggeredJSFences, long triggerTime, int transitionType);
    public abstract void onError(Context context, int errorCode, String errorMessage);

    @Override
    public void onReceive(Context context, Intent intent) {

        if(Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            handleBootCompleted(context);
        }
        else {
            handleGeofence(context, intent);
        }
    }

    private void handleBootCompleted(Context context) {

        PerimeterPlugin perimeter = new PerimeterPlugin();
        perimeter.tryGetCustomReceiver(context);
        perimeter.tryInitClient(context, true);
        if(perimeter.hasLocationPermissionsAtBoot(context)) {
            perimeter.checkForExistingFences(context);
        }
        else {
            Log.d(PERIMETER_TAG, ERROR_MESSAGES.get(PERIMETER_ERROR.INCORRECT_PERMISSIONS.ordinal()));
        }
    }

    private void handleGeofence(Context context, Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if(geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                int errorCode = geofencingEvent.getErrorCode();
                String errorMessage = GoogleApiAvailabilityLight.getInstance().getErrorString(errorCode);
                Log.e(PERIMETER_TAG, ERROR_MESSAGES.get(PERIMETER_ERROR.GENERIC_PLATFORM_ERROR));
                onError(context, errorCode, errorMessage);
                EventBus.getDefault().post(new PerimeterPlugin.PlatformEvent(errorCode, errorMessage, null));
                return;
            }

            List<Geofence> triggeredFences = geofencingEvent.getTriggeringGeofences();
            JSONArray fencesActiveWhileAdded = null;

            try {
                if(triggeredFences.size() > 0) {
                    fencesActiveWhileAdded = new JSONArray(intent.getStringExtra(Constants.ALL_ACTIVE_FENCES_EXTRA));

                    long triggeringTime = geofencingEvent.getTriggeringLocation().getTime();
                    int transitionType = geofencingEvent.getGeofenceTransition();
                    ArrayList<JSObject> triggeredJSObj = new ArrayList<JSObject>();

                    for(int i = 0; i < fencesActiveWhileAdded.length(); i++)
                    {
                        for(int k = 0; k < triggeredFences.size(); k++)
                        {
                            JSObject jsonFence = JSObject.fromJSONObject((JSONObject) fencesActiveWhileAdded.get(i));

                            if(jsonFence.getString("uid").equals(triggeredFences.get(k).getRequestId()))
                            {
                                triggeredJSObj.add(jsonFence);
                                Log.d(PERIMETER_TAG, "Fence event was successfully triggered for " + triggeredFences.get(k).getRequestId() + ".");
                            }
                        }
                    }

                    int convertedMonitor = transitionType == 1 ? MONITOR_ENTER : MONITOR_EXIT;
                    onFenceTriggered(context, triggeredJSObj, triggeringTime, convertedMonitor);
                    EventBus.getDefault().post(new PerimeterPlugin.FenceEvent(triggeredJSObj, triggeringTime, convertedMonitor));
                }
            }
            catch (JSONException e)
            {
                PerimeterPlugin.PlatformEvent parsingError = new PerimeterPlugin.PlatformEvent(ANDROID_PLATFORM_EVENT.FAILED_PARSING_INTENT_EXTRAS, null);
                Log.e(PERIMETER_TAG, parsingError.message);
                EventBus.getDefault().post(parsingError);
            }
        }
        else {
            int errorCode = PERIMETER_ERROR.GENERIC_PLATFORM_ERROR.ordinal();
            String errorMessage = ERROR_MESSAGES.get(errorCode);

            Log.e(PERIMETER_TAG, errorMessage);
            onError(context, errorCode, errorMessage);
            EventBus.getDefault().post(new PerimeterPlugin.PlatformEvent(errorCode, errorMessage, null));
        }
    }
}