const chars = 'xWvbF1_nmCasQ0.dfBghj-klqwe7rtZyu+io4pzPc';


export function encodeTries(tries: number, length = 22): string {
  const randomChar = () => chars[Math.floor(Math.random() * chars.length)];

  const targetIndex = Math.floor(Math.random() * (length - 2)) + 1;
  const arr = Array.from({ length: length - 1 }, randomChar);
  arr.unshift(chars[targetIndex]);
  arr[targetIndex] = chars[Math.min(tries - 1, chars.length - 1)];

  const checksumValue =
    arr.reduce((sum, c) => sum + c.charCodeAt(0), 0) % chars.length;
  const checksumChar = chars[checksumValue];

  return arr.join('') + checksumChar;
}

export function decodeTries(encoded: string | null): number {
  if (!encoded || encoded.length < 3) return 0;

  const dataPart = encoded.slice(0, -1); // sans checksum
  const checksumChar = encoded[encoded.length - 1];

  // Vérification du checksum
  const checksumValue =
    dataPart.split('').reduce((sum, c) => sum + c.charCodeAt(0), 0) %
    chars.length;
  const expectedChecksumChar = chars[checksumValue];

  if (checksumChar !== expectedChecksumChar) {
    console.warn('Invalid checksum: data may be corrupted.');
    return 0;
  }

  const indexChar = dataPart[0];
  const targetIndex = chars.indexOf(indexChar);

  if (targetIndex < 0 || targetIndex >= dataPart.length) {
    console.warn('Invalid target index.');
    return 0;
  }

  const codedChar = dataPart[targetIndex];
  const triesValue = chars.indexOf(codedChar);

  return triesValue >= 0 ? triesValue + 1 : 0;
}
