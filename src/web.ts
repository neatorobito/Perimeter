// Copyright Mark Raymond Jr. 2022. All Rights Reserved

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
  removeFence(_options: { fenceUID: string }): void { throw this.unimplemented(errorMessage);  }
  removeAllFences(): void { throw this.unimplemented(errorMessage); }
}
