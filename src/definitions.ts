// Copyright Mark Raymond Jr. 2022. All Rights Reserved

export interface PerimeterPlugin {
  checkPermissions(): Promise<PermissionStatus>
  requestPermissions(): Promise<PermissionStatus>
  requestForegroundPermissions(): Promise<PermissionStatus>
  requestBackgroundPermissions(): Promise<PermissionStatus>
  addFence(newFence : Fence): Promise<void>
  removeFence(options: { fenceId: string }): void
  removeAllFences(): void
}

export interface Fence {
  fenceName : string
  fenceId : string
  interests : string
  lat : number
  lng : number
  radius : number
  expires : number
  transitionType : TransitionType
}

export interface FenceNotification
{
  fenceName : string
  fenceId : number
  interests : string
  triggerTime : number
  transitionType : TransitionType
}

export const enum TransitionType {
  Enter,
  Exit,
  Both
}

export interface PermissionStatus {
  foreground: PermissionState;
  background: PermissionState;
}