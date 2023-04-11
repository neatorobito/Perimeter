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

    struct PlatformFence
    {
        let region : CLCircularRegion
        let data : Dictionary<String, Any>
    }
    
    let defaults = UserDefaults.standard
    let notificationCenter = NotificationCenter.default
    let locationManager = CLLocationManager()
    var permsCallId : String?
    var lastStatus : CLAuthorizationStatus = .notDetermined
    var activeFences = [PlatformFence]()
    
    public override func load() {
        super.load()
        notificationCenter.addObserver(self, selector: #selector(handleWillResignForeground), name: UIApplication.willResignActiveNotification, object: nil)
        notificationCenter.addObserver(self, selector: #selector(handleWillGainForeground), name: UIApplication.didBecomeActiveNotification, object: nil)
    }
    
    @objc func handleWillResignForeground(notif: Notification) {
        print("Updating geofence store before resigning foreground.")
        updateFenceStore()
    }
    
    @objc func handleWillGainForeground(notif: Notification) {
        
        // Get the fences saved from WillResignActive notification.
        let jsonFences = defaults.array(forKey: "activeFencesJSON") as? [Dictionary<String, Any>]
        let systemFences = locationManager.monitoredRegions

        var reconciledFences: [PlatformFence] = []
        var lostFencesUIDs: [String] = []
                
        // Compare to what is being monitored by CLLocationManger.

        for fenceFromSystem in systemFences {
            
            var foundFence: CLRegion? = nil
            
            if(jsonFences != nil) {
                for fenceFromSaved in jsonFences! {
                    
                    if(fenceFromSystem.identifier == fenceFromSaved["uid"]! as! String) {
                        foundFence = fenceFromSystem
                        reconciledFences.append(PlatformFence(region: fenceFromSystem as! CLCircularRegion, data: fenceFromSaved))
                    }
                }
            }

            if(foundFence == nil) {
                lostFencesUIDs.append(fenceFromSystem.identifier)
                locationManager.stopMonitoring(for: fenceFromSystem)
                print("Pruning orphaned fences.")
            }
        }
        
        if(!lostFencesUIDs.isEmpty) {
            notifyListeners("PlatformEvent", data: ["code" : Constants.Perimeter.IOS_PLATFORM_EVENT.LOST_FENCES_TO_BACKGROUND, "message" : "", "data": lostFencesUIDs], retainUntilConsumed: true)
        }
        else {
            print("No orphaned fences.")
        }
        
        // Reconcile and notify.
        
        if(!reconciledFences.isEmpty) {
            
            print("Now loading existing fences from geofence store.")
            activeFences = reconciledFences
            
            let reconciledJSON = getJSONFences(platformFences: activeFences)
            
            let platformEventDict = [
                "data" : reconciledJSON,
                "code" : Constants.Perimeter.IOS_PLATFORM_EVENT.FOREGROUND_WITH_EXISTING_FENCES.rawValue,
                "message" : ""
            ] as [String : Any]
                        
            notifyListeners("PlatformEvent", data: platformEventDict, retainUntilConsumed: true)
        }
    }
    
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
            call.reject(Constants.Perimeter.ERROR_MESSAGES[Constants.Perimeter.ERROR.GEOFENCING_UNAVAILABLE]!);
            return
        }
        else if(lastStatus != .authorizedAlways)
        {
            call.reject(Constants.Perimeter.ERROR_MESSAGES[Constants.Perimeter.ERROR.INCORRECT_PERMISSIONS]!);
            return
        }
        else if((call.getString("name") == nil) ||
                (call.getString("uid") == nil) ||
                (call.getString("payload") == nil) ||
                (call.getDouble("lat") == nil) ||
                (call.getDouble("lng") == nil) ||
                (call.getInt("radius") == nil) ||
                call.getInt("monitor") == nil) {
            
            call.reject(Constants.Perimeter.ERROR_MESSAGES[Constants.Perimeter.ERROR.INVALID_FENCE_OBJ]!);
            return
        }
        else if(!((call.getInt("radius")! >= Constants.Perimeter.MIN_FENCE_RADIUS) && (call.getInt("radius")! <= Constants.Perimeter.MAX_FENCE_RADIUS))) {
            call.reject(Constants.Perimeter.ERROR_MESSAGES[Constants.Perimeter.ERROR.INVALID_FENCE_OBJ]!)
            return
        }
        else if(activeFences.count >= Constants.Perimeter.IOS_FENCE_LIMIT) {
            call.reject(Constants.Perimeter.ERROR_MESSAGES[Constants.Perimeter.ERROR.TOO_MANY_FENCES]!);
            return
        }

        
        for fence in activeFences {
            if((fence.data["uid"] as! String) == call.getString("uid") ||
               ((fence.data["lat"] as! Double) == call.getDouble("lat") && (fence.data["lat"] as! Double) == call.getDouble("lng"))) {
                call.reject(Constants.Perimeter.ERROR_MESSAGES[Constants.Perimeter.ERROR.ALREADY_FENCED]!);
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

        let newPlatformFence = PlatformFence(
            region: newRegion,
            data: call.options! as! Dictionary<String, Any>
        )
        
        activeFences.append(newPlatformFence)
         
        locationManager.startMonitoring(for: newRegion)
        call.resolve()
        updateFenceStore()
    }
    
    @objc func removeFence(_ call: CAPPluginCall ) {
        if (!CLLocationManager.isMonitoringAvailable(for: CLCircularRegion.self)) {
            // Make sure the device supports region monitoring.
            call.reject(Constants.Perimeter.ERROR_MESSAGES[Constants.Perimeter.ERROR.GEOFENCING_UNAVAILABLE]!);
            return
        }
        else if(lastStatus != .authorizedAlways)
        {
            call.reject(Constants.Perimeter.ERROR_MESSAGES[Constants.Perimeter.ERROR.INCORRECT_PERMISSIONS]!);
            return
        }
        if(call.getString("fenceUID") == nil)
        {
            call.reject(Constants.Perimeter.ERROR_MESSAGES[Constants.Perimeter.ERROR.NO_OR_INVALID_ARGS]!);
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
            updateFenceStore()
        }
        else
        {
            call.reject(Constants.Perimeter.ERROR_MESSAGES[Constants.Perimeter.ERROR.FENCE_NOT_FOUND]!);
        }
    }
    
    @objc func removeAllFences(_ call: CAPPluginCall ) {
        
        if(activeFences.capacity <= 0)
        {
            call.resolve()
            print("There are no active fences.")
            return
        }
        
        for (index, fence) in activeFences.enumerated() {
            locationManager.stopMonitoring(for: fence.region)
            activeFences.remove(at: index)
        }
        
        call.resolve()
        updateFenceStore()
        print("Successfully removed all fences.")
    }
    
    @objc func getActiveFences(_ call: CAPPluginCall ) {
        
        let activeFencesJSON = getJSONFences(platformFences: activeFences)
        
        call.resolve(["fences" : activeFencesJSON])
    }
    
    func updateFenceStore() {
        var jsonFences: [Dictionary<AnyHashable, Any>] = []
        
        for fence in activeFences {
            jsonFences.append(fence.data)
        }
        
        defaults.set(jsonFences, forKey: "activeFencesJSON")
    }
    
    func getJSONFences(platformFences : [PlatformFence]) -> [[String : Any]] {
        var jsonFences: [[String : Any]] = []
        
        for fence in platformFences {
            jsonFences.append(fence.data)
        }
        
        return jsonFences
    }
    
    func getFenceByUID(uid : String) -> (Int, PlatformFence?)
    {
        var foundFence : PlatformFence?
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
    
    func handleFenceEvent(triggeredRegion : CLCircularRegion, eventType : Constants.Perimeter.TransitionType) {
        // This method intentionally left blank for optional low-level platform control of FenceEvents.
    }
    
    func notifyFenceEvent(triggeredRegion : CLCircularRegion, eventType : Constants.Perimeter.TransitionType)
    {
        let (resolvedFenceIndex, resolvedFence) = getFenceByUID(uid: triggeredRegion.identifier)
        let triggerTime = NSDate().timeIntervalSince1970
                
        if(resolvedFenceIndex >= 0)
        {
            print("Fence event triggered for " + triggeredRegion.identifier);
            let fenceEventDict = ["fences": [resolvedFence!.data],
                                  "time" : triggerTime,
                                  "transitionType" : eventType.rawValue ] as [String : Any]
            
            notifyListeners("FenceEvent", data: fenceEventDict)
        }
        else
        {
            // Here a fence event was triggered for a region that has been removed from our list, but never removed from CoreLocation. Try to remove it to keep order.
            locationManager.stopMonitoring(for: triggeredRegion);
        }
    }
    
    public func locationManager(_ manager: CLLocationManager, didEnterRegion region: CLRegion) {
        if let region = region as? CLCircularRegion {
            notifyFenceEvent(triggeredRegion: region, eventType: Constants.Perimeter.TransitionType.Enter)
            handleFenceEvent(triggeredRegion: region, eventType: Constants.Perimeter.TransitionType.Enter)
        }
    }
    
    public func locationManager(_ manager: CLLocationManager, didExitRegion region: CLRegion) {
        if let region = region as? CLCircularRegion {
            notifyFenceEvent(triggeredRegion: region, eventType: Constants.Perimeter.TransitionType.Exit)
            handleFenceEvent(triggeredRegion: region, eventType: Constants.Perimeter.TransitionType.Exit)
        }
    }
    
    public func locationManager(_ manager: CLLocationManager, didDetermineState state: CLRegionState, for region: CLRegion) {
        // We must override didDetermineState initially to determine if the user is inside the region as locationManager is initialized.
        if let region = region as? CLCircularRegion {
            
            if(state == .inside)
            {
                notifyFenceEvent(triggeredRegion: region, eventType: Constants.Perimeter.TransitionType.Enter)
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
