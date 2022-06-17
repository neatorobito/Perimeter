// Copyright Mark Raymond Jr., All Rights Reserved. 2022
import { registerPlugin } from '@capacitor/core';

import type { PerimeterPlugin } from './definitions';

const Perimeter = registerPlugin<PerimeterPlugin>('Perimeter', {
  web: () => import('./web').then(m => new m.PerimeterWeb()),
});

export { Fence, FenceEvent, PlatformErrorEvent, PerimeterEvent, LocationPermissionStatus, TransitionType } from './definitions';
export { Perimeter };
