import { ListenerCallback, PluginListenerHandle } from '@capacitor/core';

export interface LynxPlugin {
    setRfidMode(): Promise<void>;
    startRfidScan(): Promise<void>;
    getRFOutputPower(): Promise<{ power: number }>;
    setRFOutputPower(options: { power: number }): Promise<{ status: number }>;

    addListener(eventName: string, listenerFunc: ListenerCallback): Promise<PluginListenerHandle>;
}
