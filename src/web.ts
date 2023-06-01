import { WebPlugin } from '@capacitor/core';

import type { CapacitorUdpBroadcastPlugin } from './definitions';

export class CapacitorUdpBroadcastWeb extends WebPlugin implements CapacitorUdpBroadcastPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
