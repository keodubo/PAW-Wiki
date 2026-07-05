export function parseValidationErrors(detail: string): Record<string, string> {
  const errors: Record<string, string> = {};

  if (!detail || typeof detail !== 'string') return errors;
  const errorParts = detail
    .split(';')
    .map((part) => part.trim())
    .filter(Boolean);

  for (const part of errorParts) {
    const colonIndex = part.indexOf(':');
    if (colonIndex === -1) continue;

    const propertyPath = part.substring(0, colonIndex).trim();
    const message = part.substring(colonIndex + 1).trim();

    if (!propertyPath || !message) continue;

    const fieldName = propertyPath.split('.').pop() || propertyPath;
    errors[fieldName] = message;
  }

  return errors;
}

export function extractValidationErrors(error: any): Record<string, string> {
  if (error?.response?.data?.detail && typeof error.response.data.detail === 'string') {
    return parseValidationErrors(error.response.data.detail);
  }
  return {};
}

export function hasValidationErrors(error: any): boolean {
  return error?.response?.status === 400 && error?.response?.data?.detail && typeof error.response.data.detail === 'string';
}
