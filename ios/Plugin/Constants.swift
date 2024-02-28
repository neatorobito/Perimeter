//
//  Constants.swift
//  App
//
// Copyright Mark Raymond Jr. 2022. All Rights Reserved
//

import Foundation

struct Constants {
    
    struct Perimeter {
        
        static let IOS_FENCE_LIMIT = 20
        static let MIN_FENCE_RADIUS = 200
        static let MAX_FENCE_RADIUS = 2000
        
        enum TransitionType : Int {
            case Enter = 8, Exit = 9, Both = 10 
        }
        
        enum IOS_PLATFORM_EVENT : Int {
            case FOREGROUND_WITH_EXISTING_FENCES = 100
            case LOST_FENCES_TO_BACKGROUND
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
            FENCE_NOT_FOUND,
            TOO_MANY_FENCES
        }
        
        static let ERROR_MESSAGES : [ERROR : String] = [
            ERROR.GEOFENCING_UNAVAILABLE : "This device does not support geofencing.",
            ERROR.CLIENT_UNINITIALIZED : "Geofencing client has not been initialized : this should happen automatically after permissions are granted. Try requesting permissions again.",
            ERROR.GENERIC_PLATFORM_ERROR : "A platform specific error has occurred.",
            ERROR.INCORRECT_PERMISSIONS : "Perimeter does not have any of the required location permissions.",
            ERROR.FOREGROUND_DENIED : "This method requires foreground permissions from ACCESS_COARSE_LOCATION permissions first. Also ensure you've called Perimeter.requestForegroundPermissions before calling this method.",
            ERROR.INVALID_FENCE_OBJ : "An invalid fence object was supplied.",
            ERROR.ALREADY_FENCED : "A region with the specified UID or coordinates is already fenced.",
            ERROR.NO_OR_INVALID_ARGS : "Invalid arguments for this function.",
            ERROR.FENCE_NOT_FOUND : "A fence with that UID was not found in the list of active fences.",
            ERROR.TOO_MANY_FENCES : "Cannot exceed iOS platform limit of 20 fences. Please remove a region first and then try to add this one."
        ]
    }
}
