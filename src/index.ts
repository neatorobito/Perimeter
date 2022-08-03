// Copyright Mark Raymond Jr., All Rights Reserved. 2022
import { registerPlugin } from '@capacitor/core';

import type { PerimeterPlugin } from './definitions';

const Perimeter = registerPlugin<PerimeterPlugin>('Perimeter', {
  web: () => import('./web').then(m => new m.PerimeterWeb()),
});

export {
  LocationPermissionStatus,
  Fence,
  FenceEvent,
  PlatformEvent
} from './definitions';

export { 
  TransitionTypes,
  PerimeterErrors,
  iOSPlatformEvents,
  AndroidPlatformEvents
} from './enums'

export { Perimeter };
