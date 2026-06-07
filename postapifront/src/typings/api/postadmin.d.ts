declare namespace Api {
  namespace PostAdmin {
    type Id = string;
    type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE';
    type BodyType = 'NONE' | 'JSON' | 'XML' | 'FORM' | 'FORM_URLENCODED';

    interface Tenant {
      id: Id;
      code: string;
      name: string;
      enabled: boolean;
      createdAt?: string;
      updatedAt?: string;
    }

    interface TenantSaveRequest {
      code: string;
      name: string;
      enabled?: boolean;
    }

    interface ApiGroup {
      id: Id;
      tenantId: Id;
      name: string;
      sortOrder?: number;
      createdAt?: string;
      updatedAt?: string;
    }

    interface ApiGroupSaveRequest {
      name: string;
      sortOrder?: number;
    }

    interface ApiDefinition {
      id: Id;
      tenantId: Id;
      groupId?: Id;
      apiCode: string;
      version: string;
      apiName: string;
      method: HttpMethod;
      url: string;
      headersJson?: string;
      queryJson?: string;
      bodyType: BodyType;
      bodyTemplate?: string;
      scriptText?: string;
      encryptedSecretJson?: string;
      timeoutMillis?: number;
      enabled: boolean;
      callCount?: number;
      successCount?: number;
      failureCount?: number;
      lastCallTime?: string;
      createdAt?: string;
      updatedAt?: string;
    }

    interface ApiDefinitionSaveRequest {
      groupId?: Id;
      apiCode: string;
      version?: string;
      apiName: string;
      method: HttpMethod;
      url: string;
      headers?: Record<string, unknown>;
      query?: Record<string, unknown>;
      bodyType?: BodyType;
      bodyTemplate?: string;
      scriptText?: string;
      secret?: Record<string, unknown>;
      timeoutMillis?: number;
      enabled?: boolean;
    }

    interface ApiExecuteRequest {
      payload: Record<string, unknown>;
    }

    interface ApiExecutionResult {
      apiCode: string;
      success: boolean;
      statusCode?: number;
      responseBody?: string;
      errorMessage?: string;
      elapsedMillis: number;
      traceId?: string;
      requestDetail?: Record<string, unknown>;
    }

    interface BatchExecuteRequest {
      items: Array<{
        apiCode: string;
        payload: Record<string, unknown>;
      }>;
      stopOnFailure?: boolean;
    }

    interface BatchExecutionResult {
      batchId: string;
      total: number;
      success: number;
      failure: number;
      results: ApiExecutionResult[];
    }

    interface GroovySimulateRequest {
      script: string;
      bindings?: Record<string, unknown>;
      timeoutMillis?: number;
      allowedImports?: string[];
      blockedImports?: string[];
      blockedReceivers?: string[];
      installSecurityManager?: boolean;
    }

    interface GroovyExecutionResult {
      success: boolean;
      timeout: boolean;
      returnValue?: unknown;
      bindings?: Record<string, unknown>;
      errorType?: string;
      errorMessage?: string;
      elapsedMillis: number;
      securityManagerRequested: boolean;
      securityManagerActive: boolean;
      securityManagerMessage?: string;
      allowedImports?: string[];
      blockedImports?: string[];
      blockedReceivers?: string[];
    }

    interface ApiCallLog {
      id: Id;
      tenantId: Id;
      apiId: Id;
      apiCode: string;
      apiName: string;
      batchId?: string;
      requestPayload?: string;
      responseStatus?: number;
      responseBody?: string;
      success: boolean;
      errorMessage?: string;
      elapsedMillis?: number;
      traceId?: string;
      detailJson?: string;
      calledAt?: string;
    }

    interface Page<T> {
      records: T[];
      total: number;
      size: number;
      current: number;
      pages: number;
    }

    interface TopApiStat {
      apiCode: string;
      apiName: string;
      callCount: number;
      successCount: number;
      failureCount: number;
      lastCallTime?: string;
    }

    interface GroupStat {
      groupId: Id;
      groupName: string;
      callCount: number;
      successCount: number;
      failureCount: number;
    }

    interface LoginResult {
      tokenInfo: {
        tokenName: string;
        tokenValue: string;
        tokenTimeout?: number;
        sessionTimeout?: number;
        loginId?: Id;
      };
      tenantId: Id;
      tenantCode: string;
      username: string;
    }

    interface MeResult {
      userId: Id;
      tenantId: Id;
      tenantCode: string;
      username: string;
    }
  }
}
