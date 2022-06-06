// Copyright Mark Raymond Jr. 2022. All Rights Reserved

import Foundation
import CoreLocation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(PerimeterPlugin)
public class PerimeterPlugin: CAPPlugin, CLLocationManagerDelegate {
    
    struct PerimeterFence
    {
        let region : CLCircularRegion
        let data : Dictionary<AnyHashable, Any>
    }
    
    let locationManager = CLLocationManager()
    var permsCallId : String?
    var lastStatus : CLAuthorizationStatus = .notDetermined
    var activeFences = [PerimeterFence]()
    
    @objc public override func checkPermissions(_ call: CAPPluginCall) {
        var foreground = "prompt"
        var background = "prompt"
        
        if(locationManager.delegate == nil) {
            locationManager.delegate = self
        }
        
        lastStatus = CLLocationManager.authorizationStatus()

        switch lastStatus {
            
            case .notDetermined:
                foreground = "prompt"
                background = "prompt"
            case .restricted, .denied:
                foreground = "denied"
                background = "denied"
            case .authorizedWhenInUse:
                foreground = "granted"
                background = "prompt"
            case .authorizedAlways:
                foreground = "granted"
                background = "granted"
            @unknown default:
                foreground = "prompt"
                background = "prompt"
            }
            
        call.resolve(["foreground": foreground, "background": background])
    }

    @objc public override func requestPermissions(_ call: CAPPluginCall) {
        
        if(lastStatus == CLAuthorizationStatus.authorizedWhenInUse)
        {
            bridge?.saveCall(call)
            permsCallId = call.callbackId
            locationManager.requestAlwaysAuthorization()
        }
        else if(lastStatus == CLAuthorizationStatus.authorizedAlways)
        {
            checkPermissions(call)
            locationManager.allowsBackgroundLocationUpdates = true
        }
        else
        {
            bridge?.saveCall(call)
            permsCallId = call.callbackId
            locationManager.requestWhenInUseAuthorization()
        }
    }
    
    @objc func requestForegroundPermissions(_ call: CAPPluginCall) {
        
        if(lastStatus == CLAuthorizationStatus.notDetermined)
        {
            bridge?.saveCall(call)
            permsCallId = call.callbackId
            locationManager.requestWhenInUseAuthorization()
        }
    }
    
    @objc func requestBackgroundPermissions(_ call: CAPPluginCall) {
        if(lastStatus == CLAuthorizationStatus.authorizedWhenInUse)
        {
            bridge?.saveCall(call)
            permsCallId = call.callbackId
            locationManager.requestAlwaysAuthorization()
        }
    }
    
    @objc func addFence(_ call: CAPPluginCall ) {
        if (!CLLocationManager.isMonitoringAvailable(for: CLCircularRegion.self)) {
            // Make sure the device supports region monitoring.
            call.reject("This device does not support region monitoring.");
            return
        }
        else if(!(call.hasOption("name") &&
            call.hasOption("uid") &&
            call.hasOption("interests") &&
            call.hasOption("lat") &&
            call.hasOption("lng") &&
            call.hasOption("radius") &&
            call.hasOption("monitor"))) {
            
            call.reject("Please provide a valid fence object.");
            return
        }
        else if(lastStatus != .authorizedAlways)
        {
            call.reject("Failed to add a fence, please double check your location permissions.");
            return
        }
        
        for fence in activeFences {
            if((fence.data["uid"] as! String) == call.getString("uid") ||
               ((fence.data["lat"] as! Double) == call.getDouble("lat") && (fence.data["lat"] as! Double) == call.getDouble("lng"))) {
                call.reject("A region with the specified UID or coordinates is already fenced.");
                return;
            }
        }

        let newRegion = CLCircularRegion(
             center: CLLocationCoordinate2DMake(call.getDouble("lat")!, call.getDouble("lng")!),
             radius: call.getDouble("radius")!,
             identifier: call.getString("uid")!)
                
        let transitionType = Constants.Perimeter.TransitionType(rawValue: call.getInt("monitor")!)

        newRegion.notifyOnEntry = (transitionType == Constants.Perimeter.TransitionType.Enter ||
                                transitionType == Constants.Perimeter.TransitionType.Both) ? true : false;
        
        newRegion.notifyOnExit = (transitionType == Constants.Perimeter.TransitionType.Exit ||
                                transitionType == Constants.Perimeter.TransitionType.Both) ? true : false;

        let newFlockFence = PerimeterFence(
            region: newRegion,
            data: call.options!
        )
        
        activeFences.append(newFlockFence)
        print(call.options.description)
         
        locationManager.startMonitoring(for: newRegion)
    }
    
    @objc func removeFence(_ call: CAPPluginCall ) {
        
        if(call.getString("fenceUID") == nil)
        {
            call.reject("Please provide the identifier of a fence to remove.");
            return
        }
        else if (!CLLocationManager.isMonitoringAvailable(for: CLCircularRegion.self)) {
            // Make sure the device supports region monitoring.
            call.reject("This device does not support region monitoring.");
            return
        }
        else if(lastStatus != .authorizedAlways)
        {
            call.reject("Failed to add a fence, please double check your location permissions.");
            return
        }
        else if(activeFences.capacity <= 0)
        {
            call.resolve()
            print("There are no active fences.")
            return
        }
        
        let toRemoveId = call.getString("fenceUID")!
        let (foundFenceIndex, foundFence) = getFenceByUID(uid: toRemoveId)
        
        if(foundFenceIndex >= 0)
        {
            locationManager.stopMonitoring(for: foundFence!.region)
            activeFences.remove(at: foundFenceIndex)
            print("Successfully removed fence " + toRemoveId + ".")
            call.resolve()
        }
        else
        {
            call.reject("A fence was not found with that identifier.");
        }
    }
    
    @objc func removeAllFences(_ call: CAPPluginCall ) {
        
        for (index, fence) in activeFences.enumerated() {
            locationManager.stopMonitoring(for: fence.region)
            activeFences.remove(at: index)
        }
        
        call.resolve([:])
        print("Successfully removed all fences.")
    }
    
    func getFenceByUID(uid : String) -> (Int, PerimeterFence?)
    {
        var foundFence : PerimeterFence?
        var foundFenceIndex = -1
        
        for (index, fence) in activeFences.enumerated() {
            if(fence.data["uid"] as! String == uid)
            {
                foundFence = fence
                foundFenceIndex = index
            }
        }
        
        return (foundFenceIndex, foundFence)
    }
    
    func getJSONString(from object:Any) -> String? {
        guard let data = try? JSONSerialization.data(withJSONObject: object, options: []) else {
            print("Failed to serialize JSON data.")
            return nil
        }
        
        return String(data: data, encoding: String.Encoding.utf8)
    }
        
    func handleFenceEvent(triggeredRegion : CLCircularRegion, eventType : Constants.Perimeter.TransitionType)
    {
        let (resolvedFenceIndex, resolvedFence) = getFenceByUID(uid: triggeredRegion.identifier)
        let triggerTime = NSDate().timeIntervalSince1970
        
        if(resolvedFenceIndex >= 0)
        {
            print("Fence event triggered for " + triggeredRegion.identifier);
            
            let fenceEventDict = ["fence": resolvedFence!.data,
                             "transitionType" : eventType.rawValue,
                             "triggerTime" : triggerTime] as [String : Any]
            
            notifyListeners("FenceEvent", data: fenceEventDict)
        }
        else
        {
            print("A fence event was triggered for " + triggeredRegion.identifier);
            print("This region may have already been removed from the active list of regions.");
        }
    }
    
    public func locationManager(_ manager: CLLocationManager, didEnterRegion region: CLRegion) {
        if let region = region as? CLCircularRegion {
            handleFenceEvent(triggeredRegion: region, eventType: Constants.Perimeter.TransitionType.Enter)
        }
    }
    
    public func locationManager(_ manager: CLLocationManager, didExitRegion region: CLRegion) {
        if let region = region as? CLCircularRegion {
            handleFenceEvent(triggeredRegion: region, eventType: Constants.Perimeter.TransitionType.Exit)
        }
    }
    
    public func locationManager(_ manager: CLLocationManager, didDetermineState state: CLRegionState, for region: CLRegion) {
        // We must override didDetermineState initially to determine if the user is inside the region at that moment. didEnterRegion and didExitRegion are more about
        if let region = region as? CLCircularRegion {
            
            if(state == .inside)
            {
                handleFenceEvent(triggeredRegion: region, eventType: Constants.Perimeter.TransitionType.Enter)
            }
        }
    }
    
    public func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        
        lastStatus = status
        
        if permsCallId != nil, let call = bridge?.savedCall(withID: permsCallId!) {
            checkPermissions(call)
            
            if(lastStatus == CLAuthorizationStatus.authorizedAlways)
            {
                locationManager.allowsBackgroundLocationUpdates = true
            }
            
            bridge?.releaseCall(call)
            permsCallId = nil
        }
    }
}
