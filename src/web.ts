// Copyright Mark Raymond Jr., All Rights Reserved. 2022
import type { PluginListenerHandle } from '@capacitor/core';

import { WebPlugin } from '@capacitor/core';

import { Fence, PerimeterPlugin, LocationPermissionStatus } from './definitions';

const errorMessage = "This plugin does not have a web implementation.";

export class PerimeterWeb extends WebPlugin implements PerimeterPlugin {

  PerimeterPlugin()
  {
    throw this.unimplemented(errorMessage);
  }

  async checkPermissions(): Promise<LocationPermissionStatus> { throw this.unimplemented(errorMessage);  }
  async requestPermissions(): Promise<LocationPermissionStatus> { throw this.unimplemented(errorMessage);  }
  async requestForegroundPermissions(): Promise<LocationPermissionStatus> { throw this.unimplemented(errorMessage); }
  async requestBackgroundPermissions(): Promise<LocationPermissionStatus> { throw this.unimplemented(errorMessage); }
  async addFence(_newFence: Fence): Promise<void> { throw this.unimplemented(errorMessage);  }
  addListener( _eventName: string, _listenerFunc: (data: any) => void): Promise<PluginListenerHandle> & PluginListenerHandle { throw this.unimplemented(errorMessage); }
  removeFence(_options: { fenceUID: string }): Promise<void> { throw this.unimplemented(errorMessage);  }
  removeAllFences(): Promise<void> { throw this.unimplemented(errorMessage); }
  removeAllListeners(): Promise<void> { throw this.unimplemented(errorMessage); }
  getActiveFences(): Promise<{ data : Array<Fence> }> { throw this.unimplemented(errorMessage); }
}
