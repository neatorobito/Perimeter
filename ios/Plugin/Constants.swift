//
//  Constants.swift
//  App
//
// Copyright Mark Raymond Jr. 2022. All Rights Reserved
//

import Foundation

struct Constants {
    
    struct Perimeter {
        
        static let publishable = "org_test_pk_fc9616673797dd4211226bbc75b46eb5da2ab3ca";
        static let PERIMETER_TAG = "Perimeter";
        static let DEEP_LINK_BASE_URI = "fyi.meld.picketfence://";

        enum TransitionType : Int {
            case Enter = 0, Exit, Both
        }
    }
}
