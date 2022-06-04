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

</docgen-index>

Simple cross platform geofencing for Capacitor

## [Getting Started](docs\getting-started.md)


#### A quick note about location permissions

In 2019, Apple and Google added a layer of privacy protections by changing the user's permissions flow for apps requesting precise location access while running in the background. This API provides a set of methods that helps you handle location permissions across OS versions. Use `checkPermissions()` and `requestPermissions()` on older OS releases before a strict foreground and background permissions distinction was introduced. Use `requestForegroundPermissions()` and `requestBackgroundPermissions()` on recent releases. If you attempt to call the wrong permissions method on the wrong OS version, you will receive an error.

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
removeFence(options: { fenceUID: string; }) => void
```

Stop monitoring for a fence associated with the specified identifier. If the fence cannot be found, this method will fail and display an console error.

| Param         | Type                               |
| ------------- | ---------------------------------- |
| **`options`** | <code>{ fenceUID: string; }</code> |

--------------------


### removeAllFences()

```typescript
removeAllFences() => void
```

Stop monitoring for all active fences; stop all background location activity performed by this module.

--------------------

</docgen-api>

Copyright Mark Raymond Jr., All Rights Reserved. 2022