import { request } from '../request';

export function fetchPostAdminLogin(tenantCode: string, username: string, password: string) {
  return request<Api.PostAdmin.LoginResult>({
    url: '/auth/login',
    method: 'post',
    data: {
      tenantCode,
      username,
      password
    }
  });
}

export function fetchPostAdminLogout() {
  return request<void>({
    url: '/auth/logout',
    method: 'post'
  });
}

export function fetchPostAdminMe() {
  return request<Api.PostAdmin.MeResult>({
    url: '/auth/me',
    method: 'get'
  });
}

export function fetchTenants() {
  return request<Api.PostAdmin.Tenant[]>({
    url: '/admin/tenants',
    method: 'get'
  });
}

export function createTenant(data: Api.PostAdmin.TenantSaveRequest) {
  return request<Api.PostAdmin.Tenant>({
    url: '/admin/tenants',
    method: 'post',
    data
  });
}

export function updateTenant(id: Api.PostAdmin.Id, data: Api.PostAdmin.TenantSaveRequest) {
  return request<Api.PostAdmin.Tenant>({
    url: `/admin/tenants/${id}`,
    method: 'put',
    data
  });
}

export function deleteTenant(id: Api.PostAdmin.Id) {
  return request<void>({
    url: `/admin/tenants/${id}`,
    method: 'delete'
  });
}

export function fetchApiGroups() {
  return request<Api.PostAdmin.ApiGroup[]>({
    url: '/api/groups',
    method: 'get'
  });
}

export function createApiGroup(data: Api.PostAdmin.ApiGroupSaveRequest) {
  return request<Api.PostAdmin.ApiGroup>({
    url: '/api/groups',
    method: 'post',
    data
  });
}

export function updateApiGroup(id: Api.PostAdmin.Id, data: Api.PostAdmin.ApiGroupSaveRequest) {
  return request<Api.PostAdmin.ApiGroup>({
    url: `/api/groups/${id}`,
    method: 'put',
    data
  });
}

export function deleteApiGroup(id: Api.PostAdmin.Id) {
  return request<void>({
    url: `/api/groups/${id}`,
    method: 'delete'
  });
}

export function fetchApiDefinitions() {
  return request<Api.PostAdmin.ApiDefinition[]>({
    url: '/api/definitions',
    method: 'get'
  });
}

export function fetchApiDefinition(apiCode: string) {
  return request<Api.PostAdmin.ApiDefinition>({
    url: `/api/definitions/${apiCode}`,
    method: 'get'
  });
}

export function fetchApiDefinitionVersions(apiCode: string) {
  return request<Api.PostAdmin.ApiDefinition[]>({
    url: `/api/definitions/${apiCode}/versions`,
    method: 'get'
  });
}

export function createApiDefinition(data: Api.PostAdmin.ApiDefinitionSaveRequest) {
  return request<Api.PostAdmin.ApiDefinition>({
    url: '/api/definitions',
    method: 'post',
    data
  });
}

export function updateApiDefinition(id: Api.PostAdmin.Id, data: Api.PostAdmin.ApiDefinitionSaveRequest) {
  return request<Api.PostAdmin.ApiDefinition>({
    url: `/api/definitions/${id}`,
    method: 'put',
    data
  });
}

export function copyApiDefinition(id: Api.PostAdmin.Id) {
  return request<Api.PostAdmin.ApiDefinition>({
    url: `/api/definitions/${id}/copy`,
    method: 'post'
  });
}

export function deleteApiDefinition(id: Api.PostAdmin.Id) {
  return request<void>({
    url: `/api/definitions/${id}`,
    method: 'delete'
  });
}

export function simulateApi(apiCode: string, payload: Record<string, unknown>) {
  return request<Api.PostAdmin.ApiExecutionResult>({
    url: `/api/runtime/${apiCode}/simulate`,
    method: 'post',
    data: {
      payload
    }
  });
}

export function executeApi(apiCode: string, payload: Record<string, unknown>) {
  return request<Api.PostAdmin.ApiExecutionResult>({
    url: `/api/runtime/${apiCode}/execute`,
    method: 'post',
    data: {
      payload
    }
  });
}

export function batchExecute(data: Api.PostAdmin.BatchExecuteRequest) {
  return request<Api.PostAdmin.BatchExecutionResult>({
    url: '/api/runtime/batch',
    method: 'post',
    data
  });
}

export function simulateGroovy(data: Api.PostAdmin.GroovySimulateRequest) {
  return request<Api.PostAdmin.GroovyExecutionResult>({
    url: '/api/groovy/simulate',
    method: 'post',
    data
  });
}

export function fetchTopApiStats(limit = 10) {
  return request<Api.PostAdmin.TopApiStat[]>({
    url: '/api/stats/top',
    method: 'get',
    params: { limit }
  });
}

export function fetchGroupStats() {
  return request<Api.PostAdmin.GroupStat[]>({
    url: '/api/stats/groups',
    method: 'get'
  });
}

export function fetchApiLogs(params: { page?: number; size?: number; apiCode?: string }) {
  return request<Api.PostAdmin.Page<Api.PostAdmin.ApiCallLog>>({
    url: '/api/stats/logs',
    method: 'get',
    params
  });
}
