# Perimeter

Simple cross platform geofencing for Capacitor

## Install

```bash
npm install perimeter
npx cap sync
```

## API

<docgen-index>

* [`checkPermissions()`](#checkpermissions)
* [`requestPermissions()`](#requestpermissions)
* [`requestForegroundPermissions()`](#requestforegroundpermissions)
* [`requestBackgroundPermissions()`](#requestbackgroundpermissions)
* [`addFence(...)`](#addfence)
* [`removeFence(...)`](#removefence)
* [`removeAllFences()`](#removeallfences)

</docgen-index>

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

Prompt the user for access to their precise location at all times. **Use this method only on older OS releases before a strict foreground and background permissions distinction was introduced. Use this method on iOS if the release &lt;= iOS 13 or on Android if the release &lt;= Android 9.** Attempting to use this method on a later release will return an error.

**Returns:** <code>Promise&lt;LocationPermissionStatus&gt;</code>

--------------------


### requestForegroundPermissions()

```typescript
requestForegroundPermissions() => Promise<LocationPermissionStatus>
```

Prompt the user for access to their location while the app is running in the foreground. On iOS and Android, users must first grant this permission before your app can access their location while it is running the background. **Use this method on iOS if the release &gt;= iOS 13 or on Android if the release &gt;= Android 10.** Attempting to use this method on an older release will return an error.

**Returns:** <code>Promise&lt;LocationPermissionStatus&gt;</code>

--------------------


### requestBackgroundPermissions()

```typescript
requestBackgroundPermissions() => Promise<LocationPermissionStatus>
```

Prompt the user for access to their location while the app is running in the background. On iOS and Android, uses must first grant foreground location permissions before your app can access their location while it is running the background. **Use this method on iOS if the release &gt;= iOS 13 or on Android if the release &gt;= Android 10.** Attempting to use this method on an older release will return an error.

**Returns:** <code>Promise&lt;LocationPermissionStatus&gt;</code>

--------------------


### addFence(...)

```typescript
addFence(newFence: Fence) => Promise<void>
```

| Param          | Type               |
| -------------- | ------------------ |
| **`newFence`** | <code>Fence</code> |

--------------------


### removeFence(...)

```typescript
removeFence(options: { fenceUID: string; }) => void
```

| Param         | Type                               |
| ------------- | ---------------------------------- |
| **`options`** | <code>{ fenceUID: string; }</code> |

--------------------


### removeAllFences()

```typescript
removeAllFences() => void
```

--------------------

</docgen-api>
