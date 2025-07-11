import { registerPlugin } from '@capacitor/core';

import type { lynxPlugin } from './definitions';

const lynx = registerPlugin<lynxPlugin>('lynx', {
  web: () => import('./web').then((m) => new m.lynxWeb()),
});

export * from './definitions';
export { lynx };
