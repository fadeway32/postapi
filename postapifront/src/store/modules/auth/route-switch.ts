export function shouldFetchUserInfoOnRouteSwitch(token: string | null | undefined, userId: string | null | undefined) {
  return Boolean(token) && !userId;
}
