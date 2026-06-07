export const methodOptions: Api.PostAdmin.HttpMethod[] = ['GET', 'POST', 'PUT', 'DELETE'];
export const bodyTypeOptions: Api.PostAdmin.BodyType[] = ['NONE', 'JSON', 'XML', 'FORM', 'FORM_URLENCODED'];

export function parseJsonObject(text: string, fallback: Record<string, unknown> = {}) {
  const trimmed = text.trim();
  if (!trimmed) {
    return fallback;
  }

  const parsed = JSON.parse(trimmed);
  if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
    throw new Error('JSON must be an object');
  }

  return parsed as Record<string, unknown>;
}

export function prettyJson(value: unknown) {
  if (value === undefined || value === null || value === '') {
    return '{}';
  }

  if (typeof value === 'string') {
    try {
      return JSON.stringify(JSON.parse(value), null, 2);
    } catch {
      return value;
    }
  }

  return JSON.stringify(value, null, 2);
}

export function getPostAdminRequestErrorMessage(error: unknown, fallback = 'Failed to load data') {
  const requestError = error as {
    message?: string;
    response?: {
      data?: {
        message?: string;
        msg?: string;
      };
    };
  };

  return (
    requestError?.response?.data?.message || requestError?.response?.data?.msg || requestError?.message || fallback
  );
}

export function parseDefinitionPayload(form: {
  groupId?: Api.PostAdmin.Id;
  apiCode: string;
  version: string;
  apiName: string;
  method: Api.PostAdmin.HttpMethod;
  url: string;
  headersText: string;
  queryText: string;
  bodyType: Api.PostAdmin.BodyType;
  bodyTemplate: string;
  scriptText: string;
  secretText: string;
  timeoutMillis?: number;
  enabled: boolean;
}) {
  return {
    groupId: form.groupId,
    apiCode: form.apiCode,
    version: form.version,
    apiName: form.apiName,
    method: form.method,
    url: form.url,
    headers: parseJsonObject(form.headersText),
    query: parseJsonObject(form.queryText),
    bodyType: form.bodyType,
    bodyTemplate: form.bodyTemplate,
    scriptText: form.scriptText,
    secret: form.secretText.trim() ? parseJsonObject(form.secretText) : undefined,
    timeoutMillis: form.timeoutMillis,
    enabled: form.enabled
  } satisfies Api.PostAdmin.ApiDefinitionSaveRequest;
}
