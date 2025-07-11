# lynx

lynx rfid scanner plugin for capacitor

## Install

```bash
npm install lynx
npx cap sync
```

## API

<docgen-index>

* [`setRfidMode()`](#setrfidmode)
* [`startRfidScan()`](#startrfidscan)
* [`getRFOutputPower()`](#getrfoutputpower)
* [`setRFOutputPower(...)`](#setrfoutputpower)
* [`addListener(string, ...)`](#addlistenerstring-)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### setRfidMode()

```typescript
setRfidMode() => Promise<void>
```

--------------------


### startRfidScan()

```typescript
startRfidScan() => Promise<void>
```

--------------------


### getRFOutputPower()

```typescript
getRFOutputPower() => Promise<{ power: number; }>
```

**Returns:** <code>Promise&lt;{ power: number; }&gt;</code>

--------------------


### setRFOutputPower(...)

```typescript
setRFOutputPower(options: { power: number; }) => Promise<{ status: number; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ power: number; }</code> |

**Returns:** <code>Promise&lt;{ status: number; }&gt;</code>

--------------------


### addListener(string, ...)

```typescript
addListener(eventName: string, listenerFunc: ListenerCallback) => Promise<PluginListenerHandle>
```

| Param              | Type                                                          |
| ------------------ | ------------------------------------------------------------- |
| **`eventName`**    | <code>string</code>                                           |
| **`listenerFunc`** | <code><a href="#listenercallback">ListenerCallback</a></code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### Interfaces


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


### Type Aliases


#### ListenerCallback

<code>(err: any, ...args: any[]): void</code>

</docgen-api>
