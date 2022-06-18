// Copyright Mark Raymond Jr. 2022. All Rights Reserved

package fyi.meld.perimeter;

import static fyi.meld.perimeter.Constants.*;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@TargetApi(30)
@CapacitorPlugin(
        name = "Perimeter",
        requestCodes = { LOCATIONS_PERMISSIONS_REQUEST_CODE },
        permissions = {
                @Permission (alias = FOREGROUND_ALIAS,
                        strings = {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                        }
                ),
                @Permission (alias = BACKGROUND_ALIAS,
                        strings = {
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        }
                )
        }
)

public final class PerimeterPlugin extends Plugin {

    public static class FenceEvent {
        JSArray fences;
        long time;
        int monitor;

        public FenceEvent(ArrayList<JSObject> fences, long time, int monitor) {
            this.fences = new JSArray(fences);
            this.time = time;
            this.monitor = monitor;
        }
    }

    public static class PlatformErrorEvent {
        int errorCode;
        String errorMessage;

        public PlatformErrorEvent(int errorCode, String errorMessage) {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public PlatformErrorEvent(PERIMETER_ERROR perimeterErrorCode) {
            this.errorCode = perimeterErrorCode.ordinal();
            this.errorMessage = ERROR_MESSAGES.get(this.errorCode);
        }
    }

    private GeofencingClient geofencingClient;
    private PendingIntent fencePendingIntent;
    private ArrayList<JSObject> activeFences;
    private Class<? extends PerimeterReceiver> fenceReceiverClass;

    public PerimeterPlugin()
    {
        activeFences = new ArrayList<JSObject>();
    }

    @Override
    public void load() {
        super.load();
        tryGetCustomReceiver();
        tryInitClient();
    }

    private void tryInitClient()
    {
        if(hasLocationPermissions() && geofencingClient == null)
        {
            geofencingClient = LocationServices.getGeofencingClient(getContext());
            Log.d(PERIMETER_TAG, CLIENT_INITIALIZED);
        }
    }

    private void tryGetCustomReceiver()
    {
        Application capApp = getActivity().getApplication();

        if(capApp instanceof PerimeterApplicationHooks) {
            this.fenceReceiverClass = ((PerimeterApplicationHooks) capApp).GetCustomReceiverClass();
        }
        else
        {
            fenceReceiverClass = SimplePerimeterReceiver.class;
        }
    }

    private boolean hasForegroundPermissions() { return (getPermissionState(FOREGROUND_ALIAS) == PermissionState.GRANTED); };

    private boolean hasBackgroundPermissions() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || (getPermissionState(BACKGROUND_ALIAS) == PermissionState.GRANTED);
    };

    private boolean hasLocationPermissions()
    {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ? hasForegroundPermissions() : hasForegroundPermissions() && hasBackgroundPermissions();
    }

    @PluginMethod
    public void requestForegroundPermissions(PluginCall call) {
        requestPermissionForAlias(FOREGROUND_ALIAS, call, "locationPermissionsCallback");
    }

    @PluginMethod
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void requestBackgroundPermissions(PluginCall call) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            call.unavailable(ERROR_MESSAGES.get(PERIMETER_ERROR.FOREGROUND_DENIED));
            return;
        }
        else if(!hasForegroundPermissions())
        {
            call.reject(ERROR_MESSAGES.get(PERIMETER_ERROR.FOREGROUND_DENIED));
            return;
        }

        requestPermissionForAlias(BACKGROUND_ALIAS, call, "locationPermissionsCallback");
    }

    @Override
    @PluginMethod
    @PermissionCallback
    public void checkPermissions(PluginCall pluginCall) {
        JSObject permissionsResults = new JSObject();
        permissionsResults.put(FOREGROUND_ALIAS, getPermissionState(FOREGROUND_ALIAS));

        // Capacitor does not handle this correctly, so we need to override it below Android 10.
        PermissionState backgroundPermissionState = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ?
                PermissionState.GRANTED :
                getPermissionState(BACKGROUND_ALIAS);

        permissionsResults.put(BACKGROUND_ALIAS, backgroundPermissionState);

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
            call.reject(ERROR_MESSAGES.get(PERIMETER_ERROR.INCORRECT_PERMISSIONS), PERIMETER_ERROR.INCORRECT_PERMISSIONS.name());
            return;
        }
        else if(geofencingClient == null)
        {
            call.reject(ERROR_MESSAGES.get(PERIMETER_ERROR.CLIENT_UNINITIALIZED), PERIMETER_ERROR.CLIENT_UNINITIALIZED.name());
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
            call.reject(ERROR_MESSAGES.get(PERIMETER_ERROR.INVALID_FENCE_OBJ), PERIMETER_ERROR.INVALID_FENCE_OBJ.name());
            return;
        }

        for (JSONObject fence : activeFences) {
            if(fence.optString("uid").equals(call.getString("uid")) ||
                    (fence.optDouble("lat") == call.getDouble("lat") &&
                    fence.optDouble("lng") == call.getDouble("lng"))) {
                        call.reject(ERROR_MESSAGES.get(PERIMETER_ERROR.ALREADY_FENCED), PERIMETER_ERROR.ALREADY_FENCED.name());
                    return;
            }
        }

        TRANSITION_TYPE preferredTransitionType = TRANSITION_TYPE.values()[call.getInt("monitor")];

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
                .addOnFailureListener(e -> {
                    Log.e(PERIMETER_TAG, e.getLocalizedMessage());
                    call.reject(e.getLocalizedMessage(), PERIMETER_ERROR.GENERIC_PLATFORM_ERROR.name());
                });
        Log.d(PERIMETER_TAG, "Began monitoring for " + call.getString("uid") + ".");
    }

    @PluginMethod()
    public void removeFence(PluginCall call)
    {
        if(!hasLocationPermissions())
        {
            call.reject(ERROR_MESSAGES.get(PERIMETER_ERROR.INCORRECT_PERMISSIONS), PERIMETER_ERROR.INCORRECT_PERMISSIONS.name());
            return;
        }
        else if(geofencingClient == null)
        {
            call.reject(ERROR_MESSAGES.get(PERIMETER_ERROR.CLIENT_UNINITIALIZED), PERIMETER_ERROR.CLIENT_UNINITIALIZED.name());
            return;
        }
        else if(!call.hasOption("fenceUID"))
        {
            call.reject(ERROR_MESSAGES.get(PERIMETER_ERROR.NO_OR_INVALID_ARGS), PERIMETER_ERROR.NO_OR_INVALID_ARGS.name());
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
            call.reject(ERROR_MESSAGES.get(PERIMETER_ERROR.FENCE_NOT_FOUND), PERIMETER_ERROR.FENCE_NOT_FOUND.name());
            return;
        }
        else
        {
            ArrayList<String> fenceToRemove = new ArrayList<>();
            fenceToRemove.add(fenceUID);

            geofencingClient.removeGeofences(fenceToRemove);
            call.resolve();
            Log.d(PERIMETER_TAG, "Successfully removed fence " + fenceUID + ".");
        }
    }

    @PluginMethod()
    public void removeAllFences(PluginCall call)
    {
        if(!hasLocationPermissions())
        {
            call.reject(ERROR_MESSAGES.get(PERIMETER_ERROR.INCORRECT_PERMISSIONS), PERIMETER_ERROR.INCORRECT_PERMISSIONS.name());
            return;
        }
        else if(geofencingClient == null)
        {
            call.reject(ERROR_MESSAGES.get(PERIMETER_ERROR.CLIENT_UNINITIALIZED), PERIMETER_ERROR.CLIENT_UNINITIALIZED.name());
            return;
        }
        else if (activeFences.size() == 0)
        {
            call.resolve();
            Log.d(PERIMETER_TAG, "There are no active fences.");
            return;
        }

        ArrayList<String> activeFenceUIDs = new ArrayList<>();

        for(JSObject fence : activeFences)
        {
            activeFenceUIDs.add(fence.getString("uid"));
            Log.d(PERIMETER_TAG, fence.getString("uid"));
        }

        geofencingClient.removeGeofences(activeFenceUIDs);
        activeFences.clear();
        call.resolve();
        Log.d(PERIMETER_TAG, "Successfully removed all fences.");
    }

    private int getConvertedTransitionType(TRANSITION_TYPE transitionTypes)
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

        return preferredTransitionType;
    }

    private Geofence buildNewFence(String identifier, String type, double lat, double lang, float radiusInMeters, long expirationInMillis, int transitionTypes) {
        return new Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId(identifier)

            // Set circular region of the geofence
            .setCircularRegion(lat, lang, radiusInMeters)

            .setNotificationResponsiveness(STANDARD_GEOFENCE_RESPONSIVENESS_MILLISECONDS)

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

        if(fencePendingIntent == null)
        {
            Intent intent = new Intent(getContext(), fenceReceiverClass);

            try {
                intent.putExtra(ALL_ACTIVE_FENCES_EXTRA, new JSArray(activeFences.toArray()).toString());
            }
            catch(JSONException e)
            {
                Log.e(PERIMETER_TAG, ERROR_MESSAGES.get(PERIMETER_ERROR.ANDROID_FAILED_PACK_INTENT));
                EventBus.getDefault().post(new PlatformErrorEvent(PERIMETER_ERROR.ANDROID_FAILED_PACK_INTENT));
            }

            fencePendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
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
    protected void handleOnStart() {
        super.handleOnStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void handleOnStop() {
        super.handleOnStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFenceEvent(FenceEvent event) {
        JSObject fenceEventJS = new JSObject();

        fenceEventJS.put("fences", event.fences);
        fenceEventJS.put("time", event.time);
        fenceEventJS.put("monitor", event.monitor);

        notifyListeners("FenceEvent", fenceEventJS);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlatformErrorEvent(PlatformErrorEvent event) {
        JSObject errorEvent = new JSObject();

        errorEvent.put("errorCode", event.errorCode);
        errorEvent.put("errorMessage", event.errorMessage);

        notifyListeners("PlatformErrorEvent", errorEvent);
    }
}