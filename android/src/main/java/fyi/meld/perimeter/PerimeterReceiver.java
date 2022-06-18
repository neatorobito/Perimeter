// Copyright Mark Raymond Jr. 2022. All Rights Reserved

package fyi.meld.perimeter;

import static fyi.meld.perimeter.Constants.*;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

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

public abstract class PerimeterReceiver extends BroadcastReceiver {

    public abstract void onFenceTriggered(Context context, ArrayList<JSObject> triggeredJSFences, long triggerTime, int transitionType);
    public abstract void onError(Context context, int errorCode, String errorMessage);

    @Override
    public void onReceive(Context context, Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            int errorCode = geofencingEvent.getErrorCode();
            String errorMessage = GoogleApiAvailabilityLight.getInstance().getErrorString(errorCode);
            Log.e(PERIMETER_TAG, ERROR_MESSAGES.get(PERIMETER_ERROR.GENERIC_PLATFORM_ERROR));
            onError(context, errorCode, errorMessage);
            EventBus.getDefault().post(new PerimeterPlugin.PlatformErrorEvent(errorCode, errorMessage));
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

                onFenceTriggered(context, triggeredJSObj, triggeringTime, transitionType);
                EventBus.getDefault().post(new PerimeterPlugin.FenceEvent(triggeredJSObj, triggeringTime, transitionType));
            }
        }
        catch (JSONException e)
        {
            PerimeterPlugin.PlatformErrorEvent parsingError = new PerimeterPlugin.PlatformErrorEvent(PERIMETER_ERROR.ANDROID_FAILED_PARSING_INTENT_EXTRAS);
            Log.e(PERIMETER_TAG, parsingError.errorMessage);
            EventBus.getDefault().post(parsingError);
        }
    }
}
