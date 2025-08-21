export interface GeofencePlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
