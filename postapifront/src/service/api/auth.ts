import { request } from '../request';
import { fetchPostAdminLogin, fetchPostAdminMe } from './postadmin';

/**
 * Login
 *
 * @param userName User name
 * @param password Password
 */
export async function fetchLogin(userName: string, password: string, tenantCode = 'demo') {
  const result = await fetchPostAdminLogin(tenantCode, userName, password);

  return {
    ...result,
    data: result.data
      ? {
          token: result.data.tokenInfo.tokenValue,
          refreshToken: result.data.tokenInfo.tokenValue
        }
      : null
  };
}

/** Get user info */
export async function fetchGetUserInfo() {
  const result = await fetchPostAdminMe();

  return {
    ...result,
    data: result.data
      ? {
          userId: String(result.data.userId),
          userName: result.data.username,
          tenantId: result.data.tenantId,
          tenantCode: result.data.tenantCode,
          roles: [import.meta.env.VITE_STATIC_SUPER_ROLE],
          buttons: []
        }
      : null
  };
}

/**
 * Refresh token
 *
 * @param refreshToken Refresh token
 */
export function fetchRefreshToken(refreshToken: string) {
  return Promise.resolve({
    data: {
      token: refreshToken,
      refreshToken
    },
    error: null
  });
}

/**
 * return custom backend error
 *
 * @param code error code
 * @param msg error message
 */
export function fetchCustomBackendError(code: string, msg: string) {
  return request({ url: '/auth/error', params: { code, msg } });
}
