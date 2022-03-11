// Copyright Mark Raymond Jr. 2022. All Rights Reserved
import { registerPlugin } from '@capacitor/core';

import type { PerimeterPlugin } from './definitions';

const Perimeter = registerPlugin<PerimeterPlugin>('Perimeter', {
  web: () => import('./web').then(m => new m.PerimeterWeb()),
});

export * from './definitions';
export { Perimeter };
