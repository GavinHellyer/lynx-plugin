import { registerPlugin } from '@capacitor/core';

import type { LynxPlugin } from './definitions';

const Lynx = registerPlugin<LynxPlugin>('LynxPlugin', {
    web: () => import('./web').then((m) => new m.lynxWeb()),
});

export * from './definitions';
export { Lynx };
