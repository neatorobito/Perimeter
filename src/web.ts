// Copyright Mark Raymond Jr. 2022. All Rights Reserved

import { WebPlugin } from '@capacitor/core';

import { Fence, PerimeterPlugin, PermissionStatus } from './definitions';

const errorMessage = "This plugin does not have a web implementation.";

export class PerimeterWeb extends WebPlugin implements PerimeterPlugin {


  PerimeterPlugin()
  {
    throw this.unimplemented(errorMessage);
  }

  async checkPermissions(): Promise<PermissionStatus> { throw this.unimplemented(errorMessage);  }
  async requestPermissions(): Promise<PermissionStatus> { throw this.unimplemented(errorMessage);  }
  async requestForegroundPermissions(): Promise<PermissionStatus> { throw this.unimplemented(errorMessage); }
  async requestBackgroundPermissions(): Promise<PermissionStatus> { throw this.unimplemented(errorMessage); }
  async addFence(_newFence: Fence): Promise<void> { throw this.unimplemented(errorMessage);  }
  removeFence(_options: { fenceId: string }): void { throw this.unimplemented(errorMessage);  }
  removeAllFences(): void { throw this.unimplemented(errorMessage); }
}
