import { PluginListenerHandle, WebPlugin } from '@capacitor/core';

import type { LynxPlugin } from './definitions';

export class lynxWeb extends WebPlugin implements LynxPlugin {
    async setRfidMode(): Promise<void> {
        throw 'RFID not available under web';
    }
    async startRfidScan(): Promise<void> {
        throw 'RFID not available under web';
    }
    async getRFOutputPower(): Promise<{ power: number }> {
        throw 'RFID not available under web';
    }
    async setRFOutputPower(options: { power: number }): Promise<{ status: number }> {
        throw 'RFID not available under web: ' + options.power;
    }
    async addListener(eventName: string): Promise<PluginListenerHandle> {
        throw 'RFID not available under web: ' + eventName;
    }
}
