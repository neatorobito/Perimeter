// Copyright Mark Raymond Jr. 2022. All Rights Reserved

export interface PerimeterPlugin {
  checkPermissions(): Promise<LocationPermissionStatus>
  requestPermissions(): Promise<LocationPermissionStatus>
  requestForegroundPermissions(): Promise<LocationPermissionStatus>
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

export const enum TransitionType {
  Enter,
  Exit,
  Both
}
