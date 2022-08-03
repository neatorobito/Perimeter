// Copyright Mark Raymond Jr., All Rights Reserved. 2022
import type { PluginListenerHandle } from '@capacitor/core';
import { AndroidPlatformEvents, iOSPlatformEvents, TransitionTypes } from './enums';

export interface PerimeterPlugin {

  /**
   * Get the current foreground and background location permissions status.
   */
  checkPermissions(): Promise<LocationPermissionStatus>

  /**
   * Prompt the user for access to their precise location at all times on iOS 12 or Android 9 and earlier. Attempting to use this method on a later release will fail and display an console error.
  */
  requestPermissions(): Promise<LocationPermissionStatus>

  /**
   * Prompt the user for access to their location while the app is running in the foreground. For use on iOS 13 or on Android 10 and later.
  */
  requestForegroundPermissions(): Promise<LocationPermissionStatus>

  /**
   * Prompt the user for access to their location while the app is running in the background. For use on iOS 13 or on Android 10 and later.
  */
  requestBackgroundPermissions(): Promise<LocationPermissionStatus>

  /**
   * Request that system monitor a region defined by the newFence object. When the user enters or exits your fence, you will receive a fenceEvent.
  */
  addFence(newFence : Fence): Promise<void>

  /**
   * Stop monitoring for a fence associated with the specified identifier. If the fence cannot be found, this method will fail and display an console error.
  */
  removeFence(options: { fenceUID: string }): Promise<void>

  /**
   * Stop monitoring for all active fences; stop all background location activity performed by this module.
  */
  removeAllFences(): Promise<void>

  /**
   * Add an event listener for geofencing or platform specific error events.
   */
  addListener( eventName: string, listenerFunc: (event: any) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
  
  /**
   * Remove all geofencing event listeners
   */
  removeAllListeners(): Promise<void>;
  
  /**
   * Returns a list of all regions currently being monitored.
   */
  getActiveFences(): Promise<{ "data" : Array<Fence> }>
}

export class Fence {
  constructor (
    public name : string,
    public uid : string,
    public payload : string,
    public lat : number,
    public lng : number,
    public radius : number,
    public monitor : TransitionTypes ) {}
}

export class FenceEvent
{
  constructor (
    public fences : Array<Fence>,
    public time : number,
    public transitionType : TransitionTypes ) {}
}

export class PlatformEvent
{
  constructor (
    public code : iOSPlatformEvents | AndroidPlatformEvents,
    public message? : string,
    public data? : any ) {}
}

export class LocationPermissionStatus {
  constructor (
    public foreground: PermissionState = "prompt",
    public background: PermissionState = "prompt") {}
}