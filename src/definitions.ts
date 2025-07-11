export interface lynxPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
