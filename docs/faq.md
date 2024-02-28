# Frequently Asked Questions

### How many geofences can I create?
The library itself does not impose any limit on the number of geofences you can create. There are platform limits, however. On iOS your app can monitor up to 20 geofences while on Android your app can monitor up to 100 geofences. You can use a trigger notification or Capacitor's geolocation plugin to occasionally ping and swap out the a list of geofences to monitor.

### What are the minimum and maximum fence sizes supported?
All fences are circular regions with a minimum guaranteed trigger radius of 200 meters and a maximum trigger radius of 2000 meters across iOS and Android. 

### How can I monitor the user's location manually?
This library is designed to work with the well-supported and default `@capacitor/geo-location` plugin which can manually watch a user's location, get exact coordinates, etc.

### Is this plugin supported on the web?
No, this plugin is not supported in browser. 

### Is there a React Native/Flutter/Kotlin Multiplatform version of this plugin?
Not at this time.

### Does Perimeter support customization?
Yes, Perimeter provides low-level platform specific hooks for Android and iOS. For example, on Android your application can subclass `PerimeterReceiver` to handle events at the platform level in addition to the classic high-level `Perimeter.addListener("fenceEvent")`callback in JavaScript/Typescript. The plugin also has a `handleEntrance` and `handleExit` override on iOS. Perimeter is intended to be a robust and extendable base for your application to grow on.

### How do I convert an address to a set of coordinates?
This process is called geocoding and there are a number of solutions. You can use OpenStreetMaps, MapBox, MapQuest, Google Maps, or MapKit.Js.