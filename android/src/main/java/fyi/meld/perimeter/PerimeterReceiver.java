// Copyright Mark Raymond Jr. 2022. All Rights Reserved

package fyi.meld.perimeter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.getcapacitor.JSObject;
import com.google.android.gms.common.GoogleApiAvailabilityLight;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class PerimeterReceiver extends BroadcastReceiver {

    public abstract void onEntrance(Context context, ArrayList<JSObject> triggeredJSFences, long triggerTime);
    public abstract void onExit(Context context, ArrayList<JSObject> triggeredJSFences, long triggerTime);
    public abstract void onError(Context context, int errorCode,String errorMessage);

    @Override
    public void onReceive(Context context, Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            int errorCode = geofencingEvent.getErrorCode();
            String errorMessage = GoogleApiAvailabilityLight.getInstance().getErrorString(errorCode);
            Log.e(Constants.PERIMETER_TAG, "There was an error while processing a fence trigger event. Error details: " +  errorMessage);
            onError(context, errorCode, errorMessage);
            return;
        }

        List<Geofence> triggeredFences = geofencingEvent.getTriggeringGeofences();
        JSONArray fencesActiveWhileAdded = PerimeterPlugin.parseIntentExtras(intent);

        if(triggeredFences.size() > 0)
        {
            long triggeringTime = geofencingEvent.getTriggeringLocation().getTime();
            int transitionType = geofencingEvent.getGeofenceTransition();
            ArrayList<JSObject> triggeredJSObj = new ArrayList<JSObject>();

            try
            {
                for(int i = 0; i < fencesActiveWhileAdded.length(); i++)
                {
                    for(int k = 0; k < triggeredFences.size(); k++)
                    {
                        JSObject jsonFence = JSObject.fromJSONObject((JSONObject) fencesActiveWhileAdded.get(i));

                        if(jsonFence.getString("uid").equals(triggeredFences.get(k).getRequestId()))
                        {
                            jsonFence.put("triggerTime", triggeringTime);
                            jsonFence.put("transitionType", transitionType);
                            triggeredJSObj.add(jsonFence);
                            Log.d(Constants.PERIMETER_TAG, "Fence event was successfully triggered for " + triggeredFences.get(k).getRequestId() + ".");
                        }
                    }
                }
            }
            catch(JSONException e)
            {
                Log.d(Constants.PERIMETER_TAG, "Failed to parse intent extras while reconciling triggered and available.");
            }

            switch(transitionType)
            {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    onEntrance(context, triggeredJSObj, triggeringTime);
                    break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    onExit(context, triggeredJSObj, triggeringTime);
                    break;
                default:
                    break;
            }
        }
    }
}
