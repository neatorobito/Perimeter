// Copyright Mark Raymond Jr. 2022. All Rights Reserved

package fyi.karm.perimeter;

import static fyi.karm.perimeter.Constants.*;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
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
                                Manifest.permission.ACCESS_COARSE_LOCATION,
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
        int transitionType;

        public FenceEvent(ArrayList<JSObject> fences, long time, int transitionType) {
            this.fences = new JSArray(fences);
            this.time = time;
            this.transitionType = transitionType;
        }
    }

    public static class PlatformEvent {
        int code;
        String message;
        Object data;

        public PlatformEvent(int code, String message, JSArray data) {
            this.code = code;
            this.message = message;
            this.data = data == null ? new JSObject() : data;
        }

        public PlatformEvent(ANDROID_PLATFORM_EVENT platformEvent, JSObject data) {
            this.code = platformEvent.ordinal();
            this.message = ERROR_MESSAGES.get(this.code);
            this.data = data == null ? new JSObject() : data;
        }

        public PlatformEvent(PERIMETER_ERROR perimeterErrorCode) {
            this.code = perimeterErrorCode.ordinal();
            this.message = ERROR_MESSAGES.get(this.code);
            this.data = new JSObject();
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
        tryGetCustomReceiver(getContext());
        tryInitClient(getContext(), false);
    }

    void tryInitClient(Context context, boolean isBoot)
    {
        boolean hasPermissions = isBoot ? hasLocationPermissionsAtBoot(context) : hasLocationPermissions();

        if(hasPermissions && geofencingClient == null)
        {
            geofencingClient = LocationServices.getGeofencingClient(context);
            Log.d(PERIMETER_TAG, CLIENT_INITIALIZED);
        }
    }

    void tryGetCustomReceiver(Context context)
    {
        PerimeterApplicationHooks capApp = (PerimeterApplicationHooks) context.getApplicationContext();
        this.fenceReceiverClass = capApp.GetGeoFenceReceiverClass();
    }

    private boolean hasForegroundPermissions() { return (getPermissionState(FOREGROUND_ALIAS) == PermissionState.GRANTED); };

    private boolean hasBackgroundPermissions() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || (getPermissionState(BACKGROUND_ALIAS) == PermissionState.GRANTED);
    };

    boolean hasLocationPermissions()
    {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ? hasForegroundPermissions() : hasForegroundPermissions() && hasBackgroundPermissions();
    }

    boolean hasLocationPermissionsAtBoot(Context context) {
        boolean hasPermissions = false;

        for (String standardLocationPermission : STANDARD_LOCATION_PERMISSIONS) {
            hasPermissions = (context.checkSelfPermission(standardLocationPermission) == PackageManager.PERMISSION_GRANTED);
        }

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            hasPermissions = (context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED);
        }

        return hasPermissions;
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
        tryInitClient(getContext(), false);
    }

    @PermissionCallback
    private void backgroundPermissionsCallback(PluginCall call) {
        tryInitClient(getContext(), false);
    }

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
        else if(!((call.getInt("radius") >= MIN_FENCE_RADIUS) && (call.getInt("radius") <= MAX_FENCE_RADIUS)))  {
            call.reject(ERROR_MESSAGES.get(PERIMETER_ERROR.INVALID_FENCE_OBJ), PERIMETER_ERROR.INVALID_FENCE_OBJ.name());
            return;
        }
        else if(activeFences.size() >= ANDROID_FENCE_LIMIT) {
            call.reject(ERROR_MESSAGES.get(PERIMETER_ERROR.TOO_MANY_FENCES), PERIMETER_ERROR.TOO_MANY_FENCES.name());
            return;
        }

        for (JSObject fence : activeFences) {
            if(fence.optString("uid").equals(call.getString("uid")) ||
                    (fence.optDouble("lat") == call.getDouble("lat") &&
                    fence.optDouble("lng") == call.getDouble("lng"))) {
                        call.reject(ERROR_MESSAGES.get(PERIMETER_ERROR.ALREADY_FENCED), PERIMETER_ERROR.ALREADY_FENCED.name());
                    return;
            }
        }

        Geofence newFence = buildNewFence(
                call.getString("uid"),
                call.getString("payload"),
                call.getDouble("lat"),
                call.getDouble("lng"),
                call.getFloat("radius"),
                Geofence.NEVER_EXPIRE,
                call.getInt("monitor")
        );

        ArrayList<Geofence>fenceToAdd = new ArrayList<>();
        fenceToAdd.add(newFence);
        activeFences.add(call.getData());
        addFencesToClient(call, fenceToAdd, getContext());
    }

    @SuppressLint("MissingPermission")
    private void addFencesToClient(@Nullable PluginCall call, ArrayList<Geofence> fencesToAdd, Context context) {

        geofencingClient.addGeofences(getGeoFencingRequest(fencesToAdd), getFencePendingIntent(context))
                .addOnSuccessListener(v -> { if( call != null) { call.resolve(); }})
                .addOnFailureListener(e -> {
                    Log.e(PERIMETER_TAG, e.getLocalizedMessage());
                    if(call != null) {
                        call.reject(e.getLocalizedMessage(), PERIMETER_ERROR.GENERIC_PLATFORM_ERROR.name());
                    }
                });
        if(call != null) {
            Log.d(PERIMETER_TAG, "Began monitoring for " + call.getString("uid") + ".");
        }
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
        int foundInActive = -1;

        for(int i = 0; i < activeFences.size(); i++)
        {
            JSObject fence = activeFences.get(i);

            if(fenceUID.equals(fence.getString("uid")))
            {
                foundInActive = i;
            }
        }

        if(foundInActive == -1)
        {
            call.reject(ERROR_MESSAGES.get(PERIMETER_ERROR.FENCE_NOT_FOUND), PERIMETER_ERROR.FENCE_NOT_FOUND.name());
        }
        else
        {
            ArrayList<String> fenceToRemove = new ArrayList<>();
            fenceToRemove.add(fenceUID);
            activeFences.remove(foundInActive);

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
            String UID = fence.getString("uid");
            activeFenceUIDs.add(UID);
            Log.d(PERIMETER_TAG, UID);
        }

        geofencingClient.removeGeofences(activeFenceUIDs);
        activeFences.clear();
        call.resolve();
        Log.d(PERIMETER_TAG, "Successfully removed all fences.");
    }

    @PluginMethod()
    public void getActiveFences(PluginCall call) {
        //TODO Verify this output on the other side.
        JSObject activeFenceDict = new JSObject();
        activeFenceDict.put("fences", activeFences);
        call.resolve(activeFenceDict);
    }

    private int getConvertedTransitionType(int monitor)
    {
        return switch (monitor) {
            case MONITOR_ENTER -> Geofence.GEOFENCE_TRANSITION_ENTER;
            case MONITOR_EXIT -> Geofence.GEOFENCE_TRANSITION_EXIT;
            case MONITOR_BOTH -> Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT;
            default -> -1;
        };
    }

    private Geofence buildNewFence(String identifier, String type, double lat, double lang, float radiusInMeters, long expirationInMillis, int monitor) {
        return new Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId(identifier)

            // Set circular region of the geofence
            .setCircularRegion(lat, lang, radiusInMeters)

            //TODO Functionality : Loiter delay and expiration should be set by API consumers.

            .setNotificationResponsiveness(STANDARD_GEOFENCE_RESPONSIVENESS_MILLISECONDS)

            // Set the expiration duration of the geofence.
            .setExpirationDuration(expirationInMillis)

//            .setLoiteringDelay(STANDARD_GEOFENCE_DWELL_DELAY_MILLISECONDS)

            // Set the transition types we're interested in.
            // We track entry and exit transitions.
            .setTransitionTypes(getConvertedTransitionType(monitor))

            .build();
    }

    private PendingIntent getFencePendingIntent(Context context) {

        if(fencePendingIntent == null)
        {
            Intent intent = new Intent(context, fenceReceiverClass);

            try {
                intent.putExtra(ALL_ACTIVE_FENCES_EXTRA, new JSArray(activeFences.toArray()).toString());
            }
            catch(JSONException e)
            {
                int errorCode = ANDROID_PLATFORM_EVENT.FAILED_PACK_INTENT.getValue();
                String errorMessage = ERROR_MESSAGES.get(errorCode);
                Log.e(PERIMETER_TAG, errorMessage);
                PlatformEvent errorEvent = new PerimeterPlugin.PlatformEvent(errorCode, errorMessage, null);
                onPlatformEvent(errorEvent);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                fencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            } else {
                fencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

        }

        return fencePendingIntent;
    }

    private GeofencingRequest getGeoFencingRequest(ArrayList<Geofence> geofences) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofences);
        return builder.build();
    }

    void checkForExistingFences(Context context) {
            SharedPreferences prefs = context.getSharedPreferences("Perimeter", Context.MODE_PRIVATE);
            String stateString = prefs.getString("activeFencesJSON", "");

            if(!stateString.isEmpty() && activeFences.size() == 0) {

                try {

                    JSArray lastSavedFences = new JSArray(stateString);

                    if(lastSavedFences.length() > 0) {

                        ArrayList<Geofence> fencesToAdd = new ArrayList<>();

                        for(int i = 0; i < lastSavedFences.length(); i++) {
                            JSObject jsFence = JSObject.fromJSONObject((JSONObject) lastSavedFences.get(i));

                            Geofence newFence = buildNewFence(
                                    jsFence.getString("uid"),
                                    jsFence.getString("payload"),
                                    jsFence.getDouble("lat"),
                                    jsFence.getDouble("lng"),
                                    (float) jsFence.getDouble("radius"), // We don't care about narrowing conversion bc radius is at most one or two decimal place.
                                    Geofence.NEVER_EXPIRE,
                                    jsFence.getInt("monitor")
                            );

                            activeFences.add(jsFence);
                            fencesToAdd.add(newFence);
                        }

                        addFencesToClient(null, fencesToAdd, context);
                        int eventCode = ANDROID_PLATFORM_EVENT.FOREGROUND_WITH_EXISTING_FENCES.getValue();
                        Log.d(PERIMETER_TAG, "Now loading existing fences from geofence store.");
                        PlatformEvent event = new PerimeterPlugin.PlatformEvent(eventCode, "", lastSavedFences); // Kind of a waste of CPU cycles, but eh.
                        onPlatformEvent(event);

                    }
                    else {
                        Log.d(PERIMETER_TAG, "No orphaned fences.");
                    }

                } catch (JSONException e) {
                    int errorCode = ANDROID_PLATFORM_EVENT.FAILED_RESTORING_FENCES.getValue();
                    String errorMessage = ERROR_MESSAGES.get(errorCode);
                    Log.e(PERIMETER_TAG, errorMessage);
                    PlatformEvent errorEvent = new PerimeterPlugin.PlatformEvent(errorCode, errorMessage, null);
                    onPlatformEvent(errorEvent);
                };
            }
            else {
                Log.d(PERIMETER_TAG, "No orphaned fences.");
            }
    }

    private void saveExistingFences() {
        SharedPreferences prefs = getActivity().getSharedPreferences("Perimeter", Context.MODE_PRIVATE);

        if(activeFences.size() > 0) {
            try {
                prefs.edit()
                        .putString("activeFencesJSON", new JSArray(activeFences.toArray()).toString())
                        .apply();
                Log.d(PERIMETER_TAG, "Updating geofence store before resigning foreground.");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            };
        }
        else {
            prefs.edit()
                    .clear()
                    .apply();
        }
    }

    @Override
    protected void handleOnStart() {
        super.handleOnStart();
        checkForExistingFences(getContext());
        EventBus.getDefault().register(this);
    }

    @Override
    protected void handleOnStop() {
        super.handleOnStop();
        saveExistingFences();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFenceEvent(FenceEvent event) {
        JSObject fenceEventJS = new JSObject();

        fenceEventJS.put("fences", event.fences);
        fenceEventJS.put("time", event.time);
        fenceEventJS.put("transitionType", event.transitionType);

        notifyListeners("FenceEvent", fenceEventJS, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlatformEvent(PlatformEvent event) {
        JSObject platformEventJS = new JSObject();

        platformEventJS.put("code", event.code);
        platformEventJS.put("message", event.message);
        platformEventJS.put("data", event.data);

        notifyListeners("PlatformEvent", platformEventJS, true);
    }
}