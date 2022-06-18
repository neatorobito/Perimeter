# Perimeter

## API Reference

<docgen-index>

* [`checkPermissions()`](#checkpermissions)
* [`requestPermissions()`](#requestpermissions)
* [`requestForegroundPermissions()`](#requestforegroundpermissions)
* [`requestBackgroundPermissions()`](#requestbackgroundpermissions)
* [`addFence(...)`](#addfence)
* [`removeFence(...)`](#removefence)
* [`removeAllFences()`](#removeallfences)
* [`addListener(string, ...)`](#addlistenerstring)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

Simple cross platform geofencing for Capacitor

## [Getting Started](docs/getting-started.md)

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### checkPermissions()

```typescript
checkPermissions() => Promise<LocationPermissionStatus>
```

Get the current foreground and background location permissions status.

**Returns:** <code>Promise&lt;LocationPermissionStatus&gt;</code>

--------------------


### requestPermissions()

```typescript
requestPermissions() => Promise<LocationPermissionStatus>
```

Prompt the user for access to their precise location at all times on iOS 12 or Android 9 and earlier. Attempting to use this method on a later release will fail and display an console error.

**Returns:** <code>Promise&lt;LocationPermissionStatus&gt;</code>

--------------------


### requestForegroundPermissions()

```typescript
requestForegroundPermissions() => Promise<LocationPermissionStatus>
```

Prompt the user for access to their location while the app is running in the foreground. For use on iOS 13 or on Android 10 and later.

**Returns:** <code>Promise&lt;LocationPermissionStatus&gt;</code>

--------------------


### requestBackgroundPermissions()

```typescript
requestBackgroundPermissions() => Promise<LocationPermissionStatus>
```

Prompt the user for access to their location while the app is running in the background. For use on iOS 13 or on Android 10 and later.

**Returns:** <code>Promise&lt;LocationPermissionStatus&gt;</code>

--------------------


### addFence(...)

```typescript
addFence(newFence: Fence) => Promise<void>
```

Request that system monitor a region defined by the newFence object. When the user enters or exits your fence, you will receive a fenceEvent.

| Param          | Type               |
| -------------- | ------------------ |
| **`newFence`** | <code>Fence</code> |

--------------------


### removeFence(...)

```typescript
removeFence(options: { fenceUID: string; }) => Promise<void>
```

Stop monitoring for a fence associated with the specified identifier. If the fence cannot be found, this method will fail and display an console error.

| Param         | Type                               |
| ------------- | ---------------------------------- |
| **`options`** | <code>{ fenceUID: string; }</code> |

--------------------


### removeAllFences()

```typescript
removeAllFences() => Promise<void>
```

Stop monitoring for all active fences; stop all background location activity performed by this module.

--------------------


### addListener(string, ...)

```typescript
addListener(eventName: string, listenerFunc: (data: PerimeterEvent) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

Add an event listener for geofencing or platform specific error events.

| Param              | Type                                                                         |
| ------------------ | ---------------------------------------------------------------------------- |
| **`eventName`**    | <code>string</code>                                                          |
| **`listenerFunc`** | <code>(data: <a href="#perimeterevent">PerimeterEvent</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

Remove all geofencing event listeners

--------------------


### Interfaces


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


### Type Aliases


#### PerimeterEvent

<code>FenceEvent | PlatformErrorEvent</code>

</docgen-api>

Copyright Mark Raymond Jr., All Rights Reserved. 2022