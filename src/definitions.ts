// Copyright Mark Raymond Jr. 2022. All Rights Reserved
import type { PluginListenerHandle } from '@capacitor/core';

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
  removeFence(options: { fenceUID: string }): void

  /**
   * Stop monitoring for all active fences; stop all background location activity performed by this module.
  */
  removeAllFences(): void

  /**
   * Adds an event listener for geofencing events
   */
  addListener( eventName: "FenceEvent", listenerFunc: (data: FenceEvent) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
  
  /**
   * Removes all geofencing event listeners
   */
  removeAllListeners(): Promise<void>;
}

export class Fence {
  constructor (
    public name : string,
    public uid : string,
    public interests : string,
    public lat : number,
    public lng : number,
    public radius : number,
    public monitor : TransitionType ) {}

}

export class FenceEvent
{
  constructor (
    public fence : Fence,
    public time : number,
    public transitionType : TransitionType ) {}
}

export class LocationPermissionStatus {
  constructor (
    public foreground: PermissionState = "prompt",
    public background: PermissionState = "prompt") {}
}

export const enum TransitionType {
  Enter,
  Exit,
  Both
}
