export function shouldReloadOnMenuSelect(
  currentKey: string | null | undefined,
  selectedKey: string | null | undefined
) {
  return Boolean(currentKey) && currentKey === selectedKey;
}
