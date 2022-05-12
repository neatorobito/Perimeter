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

**Returns:** <code>Promise&lt;LocationPermissionStatus&gt;</code>

--------------------


### requestPermissions()

```typescript
requestPermissions() => Promise<LocationPermissionStatus>
```

**Returns:** <code>Promise&lt;LocationPermissionStatus&gt;</code>

--------------------


### requestForegroundPermissions()

```typescript
requestForegroundPermissions() => Promise<LocationPermissionStatus>
```

**Returns:** <code>Promise&lt;LocationPermissionStatus&gt;</code>

--------------------


### requestBackgroundPermissions()

```typescript
requestBackgroundPermissions() => Promise<LocationPermissionStatus>
```

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
