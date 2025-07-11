import { WebPlugin } from '@capacitor/core';

import type { lynxPlugin } from './definitions';

export class lynxWeb extends WebPlugin implements lynxPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
