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
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)
* [Enums](#enums)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### checkPermissions()

```typescript
checkPermissions() => Promise<PermissionStatus>
```

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

--------------------


### requestPermissions()

```typescript
requestPermissions() => Promise<PermissionStatus>
```

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

--------------------


### requestForegroundPermissions()

```typescript
requestForegroundPermissions() => Promise<PermissionStatus>
```

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

--------------------


### requestBackgroundPermissions()

```typescript
requestBackgroundPermissions() => Promise<PermissionStatus>
```

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

--------------------


### addFence(...)

```typescript
addFence(newFence: Fence) => Promise<void>
```

| Param          | Type                                    |
| -------------- | --------------------------------------- |
| **`newFence`** | <code><a href="#fence">Fence</a></code> |

--------------------


### removeFence(...)

```typescript
removeFence(options: { fenceId: string; }) => void
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ fenceId: string; }</code> |

--------------------


### removeAllFences()

```typescript
removeAllFences() => void
```

--------------------


### Interfaces


#### PermissionStatus

| Prop             | Type                                                        |
| ---------------- | ----------------------------------------------------------- |
| **`foreground`** | <code><a href="#permissionstate">PermissionState</a></code> |
| **`background`** | <code><a href="#permissionstate">PermissionState</a></code> |


#### Fence

| Prop                 | Type                                                      |
| -------------------- | --------------------------------------------------------- |
| **`fenceName`**      | <code>string</code>                                       |
| **`fenceId`**        | <code>string</code>                                       |
| **`interests`**      | <code>string</code>                                       |
| **`lat`**            | <code>number</code>                                       |
| **`lng`**            | <code>number</code>                                       |
| **`radius`**         | <code>number</code>                                       |
| **`expires`**        | <code>number</code>                                       |
| **`transitionType`** | <code><a href="#transitiontype">TransitionType</a></code> |


### Type Aliases


#### PermissionState

<code>'prompt' | 'prompt-with-rationale' | 'granted' | 'denied'</code>


### Enums


#### TransitionType

| Members     |
| ----------- |
| **`Enter`** |
| **`Exit`**  |
| **`Both`**  |

</docgen-api>
