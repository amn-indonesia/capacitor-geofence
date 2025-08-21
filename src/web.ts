import { WebPlugin } from '@capacitor/core';

import type { GeofencePlugin } from './definitions';

export class GeofenceWeb extends WebPlugin implements GeofencePlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
