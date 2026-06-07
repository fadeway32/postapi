import assert from 'node:assert/strict';
import test from 'node:test';
import { shouldFetchUserInfoOnRouteSwitch } from './route-switch';

test('should fetch user info only when a token exists and user info is empty', () => {
  assert.equal(shouldFetchUserInfoOnRouteSwitch('token', ''), true);
  assert.equal(shouldFetchUserInfoOnRouteSwitch('token', '1001'), false);
  assert.equal(shouldFetchUserInfoOnRouteSwitch('', ''), false);
});
