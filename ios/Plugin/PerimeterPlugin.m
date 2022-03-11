// Copyright Mark Raymond Jr. 2022. All Rights Reserved

#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(PerimeterPlugin, "Perimeter",
           CAP_PLUGIN_METHOD(checkPermissions, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(requestPermissions, CAPPluginReturnNone);
           CAP_PLUGIN_METHOD(requestForegroundPermissions, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(requestBackgroundPermissions, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(addFence, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(removeFence, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(removeAllFences, CAPPluginReturnPromise);
           )
