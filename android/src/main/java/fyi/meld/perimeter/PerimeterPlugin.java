// Copyright Mark Raymond Jr. 2022. All Rights Reserved

package fyi.meld.perimeter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.getcapacitor.Bridge;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@TargetApi(30)
@CapacitorPlugin(
        name = "Perimeter",
        requestCodes = { Constants.LOCATIONS_PERMISSIONS_REQUEST_CODE },
        permissions = {
                @Permission (alias = Constants.FOREGROUND_ALIAS,
                        strings = {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                        }
                ),
                @Permission (alias = Constants.BACKGROUND_ALIAS,
                        strings = {
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        }
                )
        }
)

public class PerimeterPlugin extends Plugin implements PerimeterReceiver.FenceEventHandler {

    private GeofencingClient geofencingClient;
    private PendingIntent fencePendingIntent;
    private ArrayList<JSObject> activeFences;
    private PerimeterReceiver fenceReceiver;

    public PerimeterPlugin()
    {
        activeFences = new ArrayList<JSObject>();
    }

    @Override
    public void load() {
        super.load();
        this.fenceReceiver = new PerimeterReceiver();
        fenceReceiver.setFenceEventHandler(this);
        getActivity().registerReceiver(fenceReceiver, new IntentFilter());
        tryInitClient();
    }

    private void tryInitClient()
    {
        if(hasLocationPermissions() && geofencingClient == null)
        {
            geofencingClient = LocationServices.getGeofencingClient(getContext());
            Log.d(Constants.PERIMETER_TAG, Constants.CLIENT_INITIALIZED);
        }
    }

    private boolean hasForegroundPermissions() { return (getPermissionState(Constants.FOREGROUND_ALIAS) == PermissionState.GRANTED); };

    private boolean hasBackgroundPermissions() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || (getPermissionState(Constants.BACKGROUND_ALIAS) == PermissionState.GRANTED);
    };

    private boolean hasLocationPermissions()
    {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ? hasForegroundPermissions() : hasForegroundPermissions() && hasBackgroundPermissions();
    }

    @PluginMethod
    public void requestForegroundPermissions(PluginCall call) {
        requestPermissionForAlias(Constants.FOREGROUND_ALIAS, call, "locationPermissionsCallback");
    }

    @PluginMethod
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void requestBackgroundPermissions(PluginCall call) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            call.unavailable("This method is only available on Android Q or later.");
            return;
        }
        else if(!hasForegroundPermissions())
        {
            call.reject("This method requires foreground permissions with " +
                    "ACCESS_COARSE_LOCATION permissions first." +
                    " Ensure you've called Perimeter.requestForegroundPermissions before calling this method.");
            return;
        }

        requestPermissionForAlias(Constants.BACKGROUND_ALIAS, call, "locationPermissionsCallback");
    }

    @Override
    @PluginMethod
    @PermissionCallback
    public void checkPermissions(PluginCall pluginCall) {
        JSObject permissionsResults = new JSObject();
        permissionsResults.put(Constants.FOREGROUND_ALIAS, getPermissionState(Constants.FOREGROUND_ALIAS));

        // Capacitor does not handle this correctly, so we need to override it below Android 10.
        PermissionState backgroundPermissionState = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ?
                PermissionState.GRANTED :
                getPermissionState(Constants.BACKGROUND_ALIAS);

        permissionsResults.put(Constants.BACKGROUND_ALIAS, backgroundPermissionState);

        pluginCall.resolve(permissionsResults);
    }

    @PermissionCallback
    private void locationPermissionsCallback(PluginCall call) {
        checkPermissions(call);
        tryInitClient();
    }

    @PermissionCallback
    private void backgroundPermissionsCallback(PluginCall call) {
        tryInitClient();
    }

    @SuppressLint("MissingPermission")
    @PluginMethod()
    public void addFence(PluginCall call)
    {
        if(!hasLocationPermissions())
        {
            call.reject(Constants.PERMISSION_DENIED_NOTICE);
            return;
        }
        else if(geofencingClient == null)
        {
            call.reject(Constants.CLIENT_UNINITIALIZED);
            return;
        }
        else if(!call.hasOption("name") &&
                !call.hasOption("uid") &&
                !call.hasOption("payload") &&
                !call.hasOption("lat") &&
                !call.hasOption("lng") &&
                !call.hasOption("radius") &&
                !call.hasOption("monitor"))
        {
            call.reject("Please provide a valid fence object to create a new fence.");
            return;
        }

        for (JSONObject fence : activeFences) {
            if(fence.optString("uid").equals(call.getString("uid")) ||
                    (fence.optDouble("lat") == call.getDouble("lat") &&
                    fence.optDouble("lng") == call.getDouble("lng"))) {
                    call.reject("A region with the specified UID or coordinates is already fenced.");
                    return;
            }
        }

        Constants.TRANSITION_TYPE preferredTransitionType = Constants.TRANSITION_TYPE.values()[call.getInt("monitor")];

        Geofence newFence = buildNewFence(
                call.getString("uid"),
                call.getString("payload"),
                call.getDouble("lat"),
                call.getDouble("lng"),
                call.getFloat("radius"),
                Geofence.NEVER_EXPIRE,
                getConvertedTransitionType(preferredTransitionType)
        );

        activeFences.add(call.getData());
        ArrayList<Geofence> fenceToAdd = new ArrayList<Geofence>();
        fenceToAdd.add(newFence);

        geofencingClient.addGeofences(getGeoFencingRequest(fenceToAdd), getFencePendingIntent())
                .addOnSuccessListener(v -> call.resolve())
                .addOnFailureListener(e -> call.reject("Failed to create fence.", e));

        Log.d(Constants.PERIMETER_TAG, "Began monitoring for " + call.getString("uid") + ".");
    }

    @PluginMethod()
    public void removeFence(PluginCall call)
    {
        if(!hasLocationPermissions())
        {
            call.reject(Constants.PERMISSION_DENIED_NOTICE);
            return;
        }
        else if(geofencingClient == null)
        {
            call.reject(Constants.CLIENT_UNINITIALIZED);
            return;
        }
        else if(!call.hasOption("fenceUID"))
        {
            call.reject("Please provide the uid of a fence to remove.");
            return;
        }

        String fenceUID = call.getString("fenceUID");
        boolean foundInActive = false;

        for(JSObject fence : activeFences)
        {
            if(fenceUID.equals(fence.getString("uid")))
            {
                foundInActive = true;
            }
        }

        if(!foundInActive)
        {
            call.reject("A fence with that id was not found in the list of active fences.");
            return;
        }
        else
        {
            ArrayList<String> fenceToRemove = new ArrayList<>();
            fenceToRemove.add(fenceUID);

            geofencingClient.removeGeofences(fenceToRemove);
            call.resolve();
            Log.d(Constants.PERIMETER_TAG, "Successfully removed fence " + fenceUID + ".");
        }
    }

    @PluginMethod()
    public void removeAllFences(PluginCall call)
    {
        if(!hasLocationPermissions())
        {
            call.reject(Constants.PERMISSION_DENIED_NOTICE);
            return;
        }
        else if(geofencingClient == null)
        {
            call.reject(Constants.CLIENT_UNINITIALIZED);
            return;
        }
        else if (activeFences.size() == 0)
        {
            call.resolve();
            Log.d(Constants.PERIMETER_TAG, "There are no active fences.");
            return;
        }

        ArrayList<String> activeFenceUIDs = new ArrayList<>();

        for(JSObject fence : activeFences)
        {
            activeFenceUIDs.add(fence.getString("uid"));
            Log.d(Constants.PERIMETER_TAG, fence.getString("uid"));
        }

        geofencingClient.removeGeofences(activeFenceUIDs);
        activeFences.clear();
        call.resolve();
        Log.d(Constants.PERIMETER_TAG, "Successfully removed all fences.");
    }

    private int getConvertedTransitionType(Constants.TRANSITION_TYPE transitionTypes)
    {
        int preferredTransitionType = Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT;

        switch (transitionTypes)
        {
            case ENTER:
                preferredTransitionType = Geofence.GEOFENCE_TRANSITION_ENTER;
                break;

            case EXIT:
                preferredTransitionType = Geofence.GEOFENCE_TRANSITION_EXIT;
                break;

            case BOTH:
                preferredTransitionType = Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT;
                break;
        }

        Log.d(Constants.PERIMETER_TAG, "TransitionType" + preferredTransitionType);

        return preferredTransitionType;
    }

    public static JSONArray parseIntentExtras(Intent intent)
    {
        JSONArray parsedExtras = null;

        try {
            parsedExtras = new JSONArray(intent.getStringExtra(Constants.ALL_ACTIVE_FENCES_EXTRA));
        }
        catch(JSONException e)
        {
            Log.d(Constants.PERIMETER_TAG, "Failed to parse data from the intent's extras.");
            System.exit(1);
        }

        return parsedExtras;
    }

    private Geofence buildNewFence(String identifier, String type, double lat, double lang, float radiusInMeters, long expirationInMillis, int transitionTypes) {
        return new Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId(identifier)

            // Set circular region of the geofence
            .setCircularRegion(lat, lang, radiusInMeters)

            .setNotificationResponsiveness(Constants.STANDARD_GEOFENCE_RESPONSIVENESS_MILLISECONDS)

            // Set the expiration duration of the geofence.
            .setExpirationDuration(expirationInMillis)

            // Set the transition types we're interested in.
            // We track entry and dwell(pause) transitions.
            //TODO Functionality : Transition types should be set by arguments to the function call so that we can support multiple types of transitions.
            .setTransitionTypes(transitionTypes)

            .build();
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private PendingIntent getFencePendingIntent() {

        if(fenceReceiver != null)
        {
            if(fencePendingIntent == null)
            {
                Intent intent = new Intent(getContext(), fenceReceiver.getClass());

                try {
                    intent.putExtra(Constants.ALL_ACTIVE_FENCES_EXTRA, new JSArray(activeFences.toArray()).toString());
                }
                catch(JSONException e)
                {
                    Log.d(Constants.PERIMETER_TAG, "Failed to pack intent data when creating a new fence.");
                    System.exit(1);
                }

                fencePendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }
        else
        {
            throw new NullPointerException("fenceReceiver is null, no class that implements PerimeterReceiver was provided.");
        }

        return fencePendingIntent;
    }

    private GeofencingRequest getGeoFencingRequest(ArrayList<Geofence> geofences) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofences);
        return builder.build();
    }

    @Override
    public void onFenceTriggered(Context context, ArrayList<JSObject> triggeredJSFences, long triggerTime, int transitionType) {

        JSObject fenceEvent = new JSObject();
        fenceEvent.put("fences", triggeredJSFences);
        fenceEvent.put("time", triggerTime);
        fenceEvent.put("transitionType", transitionType);

        notifyListeners("FenceEvent", fenceEvent);
    }

    @Override
    public void onError(Context context, int errorCode, String errorMessage) {

    }
}