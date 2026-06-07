import assert from 'node:assert/strict';
import test from 'node:test';
import { shouldReloadOnMenuSelect } from './menu-selection';

test('should reload when selecting the active menu item again', () => {
  assert.equal(shouldReloadOnMenuSelect('postadmin_definitions', 'postadmin_definitions'), true);
  assert.equal(shouldReloadOnMenuSelect('postadmin_definitions', 'postadmin_groups'), false);
  assert.equal(shouldReloadOnMenuSelect('', 'postadmin_definitions'), false);
});
