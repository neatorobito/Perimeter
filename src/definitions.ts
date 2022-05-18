// Copyright Mark Raymond Jr. 2022. All Rights Reserved

export interface PerimeterPlugin {

  /**
   * Get the current foreground and background location permissions status.
   */
  checkPermissions(): Promise<LocationPermissionStatus>

  /**
   * Prompt the user for access to their precise location at all times. **Use this method only on older OS releases before a strict foreground and background permissions distinction was introduced. Use this method on iOS if the release <= iOS 13 or on Android if the release <= Android 9.** Attempting to use this method on a later release will return an error.
  */
  requestPermissions(): Promise<LocationPermissionStatus>

  /**
   * Prompt the user for access to their location while the app is running in the foreground. On iOS and Android, users must first grant this permission before your app can access their location while it is running the background. **Use this method on iOS if the release >= iOS 13 or on Android if the release >= Android 10.** Attempting to use this method on an older release will return an error.
  */
  requestForegroundPermissions(): Promise<LocationPermissionStatus>

  /**
   * Prompt the user for access to their location while the app is running in the background. On iOS and Android, uses must first grant foreground location permissions before your app can access their location while it is running the background. **Use this method on iOS if the release >= iOS 13 or on Android if the release >= Android 10.** Attempting to use this method on an older release will return an error.
  */
  requestBackgroundPermissions(): Promise<LocationPermissionStatus>
  addFence(newFence : Fence): Promise<void>
  removeFence(options: { fenceUID: string }): void
  removeAllFences(): void
}

export class Fence {
  constructor (
    public name : string,
    public uid : string,
    public interests : string,
    public lat : number,
    public lng : number,
    public radius : number,
    public expires : number,
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

/**
 * Nonitor
*/
export const enum TransitionType {
  Enter,
  Exit,
  Both
}
