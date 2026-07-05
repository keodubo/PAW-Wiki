export interface JwtPayload {
  userId: number;
  role: string | string[];
  sub: string;
  iat: number;
  exp: number;
}

export class InvalidJwtError extends Error {
  constructor(message: string = 'Invalid JWT token') {
    super(message);
    this.name = 'InvalidJwtError';
  }
}

export function isValidJwtPayload(decoded: unknown): decoded is JwtPayload {
  if (typeof decoded !== 'object' || decoded === null) return false;
  const payload = decoded as Record<string, unknown>;

  if (typeof payload.sub !== 'string') return false;
  if (typeof payload.userId !== 'number') return false;
  if (typeof payload.iat !== 'number') return false;
  if (typeof payload.exp !== 'number') return false;
  if (typeof payload.role === 'string') return true;
  if (Array.isArray(payload.role)) return payload.role.every((r) => typeof r === 'string');

  return false;
}

export function decodeAndValidateJwt(token: string): JwtPayload {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) throw new InvalidJwtError('JWT token must have 3 parts');

    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');

    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join(''),
    );

    const decoded = JSON.parse(jsonPayload);
    if (!isValidJwtPayload(decoded)) throw new InvalidJwtError('JWT payload does not match expected structure');
    return decoded;
  } catch (error) {
    if (error instanceof InvalidJwtError) throw error;
    throw new InvalidJwtError(`Failed to decode JWT token: ${error instanceof Error ? error.message : 'Unknown error'}`);
  }
}

export function extractRole(payload: JwtPayload): string {
  if (Array.isArray(payload.role)) return payload.role[0];
  return payload.role;
}
