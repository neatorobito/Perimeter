# Perimeter

![Device Screenshots](device-shots.png)

This API provides a simple, straightforward, and robust way to do geofencing on iOS and Android. 

## Getting Started
```bash
npx cap sync
npx cap open ios #or android
```

### Declaring capabilities

Geofencing relies on the device to passively monitor the user's precise location in the background. Per Apple and Google guidelines, your app must declare that it needs this background location access and show a UI to help users understand why. On Android you must manually show this UI. 

#### iOS

On iOS, simply declare the capabilities:

![iOS Background Location](ios_bg_location.png)

Then add a description for the following property strings:

`Privacy - Location Always and When In Use Usage Description`

`Privacy - Location Always Usage Description`

`Privacy - Location When In Use Usage Description`

#### Android

1. Create a BroadcastReceiver that extends `PerimeterReceiver`.
```java
public class SimpleGeofenceReceiver extends PerimeterReceiver {
    @Override
    public void onFenceTriggered(Context context, ArrayList<JSObject> triggeredJSFences, long triggerTime, int transitionType) {

    }

    @Override
    public void onError(Context context, int errorCode, String errorMessage) {

    }
}
```

2. Next, create an Application class that implements `PerimeterApplicationsHooks`.
```java
public class MyCustomApp extends Application implements PerimeterApplicationHooks {

    @Override
    public Class<? extends PerimeterReceiver> GetGeoFenceReceiverClass() {
        return SimpleGeofenceReceiever.class;
    }

}
```

Add the following to your AndroidManifest.xml:

```xml
    <uses-feature android:name="android.hardware.location.gps" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <receiver
        android:name=".SimpleGeofenceReceiver"
        android:enabled="true"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.BOOT_COMPLETED" />
        </intent-filter>
    </receiver>
```

### Requesting Permissions

#### iOS 12 or Android 9 and earlier

```javascript
async requestPerms() : Promise<void> {
      this.permStatus = await Perimeter.checkPermissions();
    if(this.permStatus.foreground != "granted" || this.permStatus.background != "granted") {
        this.permStatus = await Perimeter.requestPermissions();
    }
}
```

#### iOS 13 or Android 10 and later
```javascript
async requestPerms() : Promise<void> {
      this.permStatus = await Perimeter.checkPermissions();
    if(this.permStatus.foreground != "granted") {
        this.permStatus = await Perimeter.requestForegroundPermissions();
    }

    if (this.permStatus.background != "granted") {
        this.permStatus = await Perimeter.requestBackgroundPermissions();
    }
}
```

Before you call `requestBackgroundPermissions` on Android, show a dialog to help users understand why the app needs their background location. **If you do not show this dialog, your app may be rejected in review.** For more information, see the following [developer docs.](https://developer.android.com/training/location/permissions#background-dialog-target-android-11)

### Creating and monitoring geofences

Start by setting up a listener for a `FenceEvent`:

```javascript
Perimeter.addListener("FenceEvent", (fenceEvent) => { 
    console.log(fenceEvent.fence.payload) 
});
```

Next, create a `Fence` object covering a given region.

```javascript
let extraData = "I want to visit this place someday.";
let newFence : Fence = {
    name : "Taj Mahal",
    uid : "123456789",
    payload: extraData,
    lat : 27.1751,
    lng : 78.0421,
    radius : 200, 
    monitor : TransitionType.Enter
};
```

The name, UID, latitude, and longitude fields are self explanatory. For the other fields:

* `payload`: A extra field containing `String` data that you can use. It will be delivered back to you when a `FenceEvent` is triggered.
* `radius`: The radius of the circular region to be monitored in meters. Across iOS and Android the minimum is 100m.
* `monitor`: Specify the `TransitionType` that will trigger a `FenceEvent`.

Finally, call `addFence(newFence)` to begin monitoring the region.

```javascript
Perimeter.addFence(newFence).then(() => {
    this.activeFences.push(newFence);
})
    .catch((e) => {
    console.log(e);
});
```

When the user enters and/or exits the region, you'll get a `FenceEvent` that looks something like this:
```javascript
FenceEvent {
    fences : 'A list of fences that were triggered by a given action',
    time : 'Trigger time as a UNIX timestamp',
    transitionType : 'The type of action that triggered this event (enter or exit)'
};
```

To show a push notification on a FenceEvent install the `@capacitor/local-notifications` plugin.
```javascript
      Perimeter.addListener("FenceEvent", (event: any) => { 
        let fenceEvent = (event as FenceEvent)
        let fenceNames = ""
        for(let fence of fenceEvent.fences) {
          fenceNames += fence.name + ' '
        }

        LocalNotifications.schedule({ 
          notifications : [{ 
            id: 123,
            title: 'Geofencing Event',
            body : `Did you ${ fenceEvent.transitionType === TransitionTypes.Enter ? 'enter' : 'exit' } ${ fenceNames.trimEnd() }?`}]})
      })
```

Keep in mind that iOS and Android use different background processing models. iOS wakes your entire app from the background so the main process can trigger the LocalNotifications plugin. On Android however, you'll need to use a Broadcast Receiver if you want to show notifications while the app is closed. For an example, replace `SimpleGeofenceReceiever` with `CustomPushGeofenceReceiver` in the sample PicketFence app. 

If you need to do additional background processing on iOS in Swift, add your code to the following method in the KarmPerimeter under DevelopmentPods in Xcode.
`func handleFenceEvent(triggeredRegion : CLCircularRegion, eventType : Constants.Perimeter.TransitionType)`


### Other important information
* Geofences are cleared after a reboot on Android. Perimeter will try to automatically handle this by reloading fences from a saved store.

Copyright Mark Raymond Jr., All Rights Reserved. 2024
