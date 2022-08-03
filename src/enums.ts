export enum TransitionTypes {
    Enter,
    Exit,
    Both
  }
  
export enum PerimeterErrors {
    GEOFENCING_UNAVAILABLE,
    CLIENT_UNINITIALIZED,
    GENERIC_PLATFORM_ERROR,
    INCORRECT_PERMISSIONS,
    FOREGROUND_DENIED,
    INVALID_FENCE_OBJ,
    ALREADY_FENCED,
    NO_OR_INVALID_ARGS,
    FENCE_NOT_FOUND,
}

export enum iOSPlatformEvents {
    FOREGROUND_WITH_EXISTING_FENCES = 100,
}

export enum AndroidPlatformEvents {
    TEST_EVENT = 200
}
