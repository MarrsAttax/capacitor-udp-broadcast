export interface CapacitorUdpBroadcastPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
