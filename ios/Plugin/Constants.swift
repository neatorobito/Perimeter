//
//  Constants.swift
//  App
//
// Copyright Mark Raymond Jr. 2022. All Rights Reserved
//

import Foundation

struct Constants {
    
    struct Perimeter {
        
        enum TransitionType : Int {
            case Enter = 1, Exit, Both
        }
        
        enum IOS_PLATFORM_EVENT : Int {
            case FOREGROUND_WITH_EXISTING_FENCES = 100
        }
        
        enum ERROR : Int {
            case GEOFENCING_UNAVAILABLE = 0,
            CLIENT_UNINITIALIZED,
            GENERIC_PLATFORM_ERROR,
            INCORRECT_PERMISSIONS,
            FOREGROUND_DENIED,
            INVALID_FENCE_OBJ,
            ALREADY_FENCED,
            NO_OR_INVALID_ARGS,
            FENCE_NOT_FOUND
        }
        
        static let ERROR_MESSAGES : [ERROR : String] = [
            ERROR.GEOFENCING_UNAVAILABLE : "This device does not support geofencing.",
            ERROR.CLIENT_UNINITIALIZED : "Geofencing client has not been initialized : this should happen automatically after permissions are granted. Try requesting permissions again.",
            ERROR.GENERIC_PLATFORM_ERROR : "A platform specific error has occurred.",
            ERROR.INCORRECT_PERMISSIONS : "Perimeter does not have any of the required location permissions.",
            ERROR.FOREGROUND_DENIED : "This method requires foreground permissions from ACCESS_COARSE_LOCATION permissions first. Also ensure you've called Perimeter.requestForegroundPermissions before calling this method.",
            ERROR.INVALID_FENCE_OBJ : "Perimeter does not have any of the required location permissions.",
            ERROR.ALREADY_FENCED : "A region with the specified UID or coordinates is already fenced.",
            ERROR.NO_OR_INVALID_ARGS : "Invalid arguments for this function.",
            ERROR.FENCE_NOT_FOUND : "A fence with that UID was not found in the list of active fences."
        ]
    }
}
