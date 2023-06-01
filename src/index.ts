import { registerPlugin } from '@capacitor/core';

import type { CapacitorUdpBroadcastPlugin } from './definitions';

const CapacitorUdpBroadcast = registerPlugin<CapacitorUdpBroadcastPlugin>('CapacitorUdpBroadcast', {
  web: () => import('./web').then(m => new m.CapacitorUdpBroadcastWeb()),
});

export * from './definitions';
export { CapacitorUdpBroadcast };
