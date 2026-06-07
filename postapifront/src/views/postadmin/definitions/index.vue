<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue';
import {
  copyApiDefinition,
  createApiDefinition,
  deleteApiDefinition,
  executeApi,
  fetchApiDefinition,
  fetchApiDefinitions,
  fetchApiDefinitionVersions,
  fetchApiGroups,
  simulateApi,
  updateApiDefinition
} from '@/service/api';
import {
  bodyTypeOptions,
  getPostAdminRequestErrorMessage,
  methodOptions,
  parseDefinitionPayload,
  parseJsonObject,
  prettyJson
} from '../shared';

defineOptions({ name: 'postadmin_definitions' });

const loading = ref(false);
const loadError = ref('');
const saving = ref(false);
const copyingId = ref<Api.PostAdmin.Id>();
const drawerVisible = ref(false);
const detailVisible = ref(false);
const historyVisible = ref(false);
const runnerVisible = ref(false);
const tableRenderKey = ref(0);
const definitions = ref<Api.PostAdmin.ApiDefinition[]>([]);
const groups = ref<Api.PostAdmin.ApiGroup[]>([]);
let loadDataRequestId = 0;
const editingId = ref<Api.PostAdmin.Id>();
const needRefreshAfterDrawerClose = ref(false);
const keyword = ref('');
const groupFilter = ref<Api.PostAdmin.Id>();
const methodFilter = ref<Api.PostAdmin.HttpMethod>();
const enabledFilter = ref<boolean>();
const detailLoading = ref(false);
const historyLoading = ref(false);
const running = ref(false);
const currentDefinition = ref<Api.PostAdmin.ApiDefinition>();
const versionHistory = ref<Api.PostAdmin.ApiDefinition[]>([]);
const runMode = ref<'simulate' | 'execute'>('simulate');
const runPayloadText = ref('{\n  "keyword": "demo"\n}');
const runResult = ref<Api.PostAdmin.ApiExecutionResult>();

function isEmptyFilterValue(value: unknown) {
  return value === undefined || value === null || value === '';
}

const form = reactive({
  groupId: undefined as Api.PostAdmin.Id | undefined,
  apiCode: '',
  version: 'v1',
  apiName: '',
  method: 'GET' as Api.PostAdmin.HttpMethod,
  url: '',
  headersText: '{}',
  queryText: '{}',
  bodyType: 'NONE' as Api.PostAdmin.BodyType,
  bodyTemplate: '',
  scriptText: '',
  secretText: '',
  timeoutMillis: undefined as number | undefined,
  enabled: true
});

const filteredDefinitions = computed(() => {
  const text = keyword.value.trim().toLowerCase();

  if (
    !text &&
    isEmptyFilterValue(groupFilter.value) &&
    isEmptyFilterValue(methodFilter.value) &&
    isEmptyFilterValue(enabledFilter.value)
  ) {
    return definitions.value;
  }

  return definitions.value.filter(item => {
    const matchedText =
      !text ||
      [item.apiCode, item.apiName, item.method, item.url].some(value =>
        String(value || '')
          .toLowerCase()
          .includes(text)
      );
    const matchedGroup = isEmptyFilterValue(groupFilter.value) || item.groupId === groupFilter.value;
    const matchedMethod = isEmptyFilterValue(methodFilter.value) || item.method === methodFilter.value;
    const matchedEnabled = isEmptyFilterValue(enabledFilter.value) || item.enabled === enabledFilter.value;

    return matchedText && matchedGroup && matchedMethod && matchedEnabled;
  });
});

const runResultBody = computed(() => runResult.value?.responseBody || runResult.value?.errorMessage || '');

function groupName(groupId?: Api.PostAdmin.Id) {
  return groups.value.find(item => item.id === groupId)?.name || 'Ungrouped';
}

async function loadData() {
  const requestId = loadDataRequestId + 1;
  loadDataRequestId = requestId;

  loading.value = true;
  loadError.value = '';

  try {
    const [definitionResult, groupResult] = await Promise.all([fetchApiDefinitions(), fetchApiGroups()]);
    if (requestId !== loadDataRequestId) return;

    if (definitionResult.error) {
      loadError.value = getPostAdminRequestErrorMessage(definitionResult.error, 'Failed to load API definitions');
      return;
    }

    definitions.value = [...definitionResult.data];

    if (!groupResult.error) {
      groups.value = [...groupResult.data];
    }

    tableRenderKey.value += 1;
    await nextTick();
  } finally {
    if (requestId === loadDataRequestId) {
      loading.value = false;
    }
  }
}

function upsertDefinition(definition: Api.PostAdmin.ApiDefinition) {
  const index = definitions.value.findIndex(item => item.id === definition.id);
  if (index >= 0) {
    definitions.value.splice(index, 1, definition);
    return;
  }

  definitions.value.unshift(definition);
}

function resetForm() {
  Object.assign(form, {
    groupId: undefined,
    apiCode: '',
    version: 'v1',
    apiName: '',
    method: 'GET',
    url: '',
    headersText: '{}',
    queryText: '{}',
    bodyType: 'NONE',
    bodyTemplate: '',
    scriptText: '',
    secretText: '',
    timeoutMillis: undefined,
    enabled: true
  });
}

function openCreate() {
  editingId.value = undefined;
  resetForm();
  drawerVisible.value = true;
}

function openEdit(row: Api.PostAdmin.ApiDefinition) {
  editingId.value = row.id;
  Object.assign(form, {
    groupId: row.groupId,
    apiCode: row.apiCode,
    version: row.version || 'v1',
    apiName: row.apiName,
    method: row.method,
    url: row.url,
    headersText: prettyJson(row.headersJson),
    queryText: prettyJson(row.queryJson),
    bodyType: row.bodyType || 'NONE',
    bodyTemplate: row.bodyTemplate || '',
    scriptText: row.scriptText || '',
    secretText: '',
    timeoutMillis: row.timeoutMillis,
    enabled: row.enabled
  });
  drawerVisible.value = true;
}

async function openHistory(row: Api.PostAdmin.ApiDefinition) {
  historyVisible.value = true;
  historyLoading.value = true;
  versionHistory.value = [];
  try {
    const { data, error } = await fetchApiDefinitionVersions(row.apiCode);
    versionHistory.value = error ? [row] : data;
  } finally {
    historyLoading.value = false;
  }
}

async function openDetail(row: Api.PostAdmin.ApiDefinition) {
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    const { data, error } = await fetchApiDefinition(row.apiCode);
    currentDefinition.value = error ? row : data;
  } finally {
    detailLoading.value = false;
  }
}

function openRunner(row: Api.PostAdmin.ApiDefinition, mode: 'simulate' | 'execute') {
  currentDefinition.value = row;
  runMode.value = mode;
  runResult.value = undefined;
  runnerVisible.value = true;
}

async function save() {
  if (!form.apiCode.trim() || !form.apiName.trim() || !form.url.trim()) {
    window.$message?.warning('Code, name and URL are required');
    return;
  }

  let payload: Api.PostAdmin.ApiDefinitionSaveRequest;
  try {
    payload = parseDefinitionPayload(form);
  } catch (error) {
    window.$message?.error((error as Error).message);
    return;
  }

  saving.value = true;
  const result = editingId.value
    ? await updateApiDefinition(editingId.value, payload)
    : await createApiDefinition(payload);
  saving.value = false;

  if (!result.error) {
    if (result.data) {
      upsertDefinition(result.data);
    }

    needRefreshAfterDrawerClose.value = true;
    drawerVisible.value = false;
    window.$message?.success('Saved');
  }
}

async function handleDrawerClosed() {
  editingId.value = undefined;
  resetForm();

  if (!needRefreshAfterDrawerClose.value) {
    return;
  }

  needRefreshAfterDrawerClose.value = false;
  await loadData();
}

async function runApi() {
  const api = currentDefinition.value;
  if (!api) return;

  let payload: Record<string, unknown>;
  try {
    payload = parseJsonObject(runPayloadText.value);
  } catch (error) {
    window.$message?.error((error as Error).message);
    return;
  }

  running.value = true;
  const result =
    runMode.value === 'simulate' ? await simulateApi(api.apiCode, payload) : await executeApi(api.apiCode, payload);
  running.value = false;

  if (!result.error) {
    runResult.value = result.data;
    window.$message?.success(runMode.value === 'simulate' ? 'Simulated' : 'Executed');
    await loadData();
  }
}

async function remove(row: Api.PostAdmin.ApiDefinition) {
  await window.$messageBox?.confirm(`Delete API "${row.apiCode}"?`, 'Confirm', { type: 'warning' });
  const { error } = await deleteApiDefinition(row.id);
  if (!error) {
    window.$message?.success('Deleted');
    await loadData();
  }
}

async function copyDefinition(row: Api.PostAdmin.ApiDefinition) {
  copyingId.value = row.id;
  try {
    const { data, error } = await copyApiDefinition(row.id);
    if (!error && data) {
      upsertDefinition(data);
      tableRenderKey.value += 1;
      window.$message?.success('Copied');
    }
  } finally {
    copyingId.value = undefined;
  }
}

function copyCode(row: Api.PostAdmin.ApiDefinition) {
  navigator.clipboard?.writeText(row.apiCode);
  window.$message?.success('Copied');
}

function resetFilters() {
  keyword.value = '';
  groupFilter.value = undefined;
  methodFilter.value = undefined;
  enabledFilter.value = undefined;
}

async function reloadRouteData() {
  resetFilters();
  currentDefinition.value = undefined;
  runResult.value = undefined;
  await loadData();
}

onMounted(reloadRouteData);
</script>

<template>
  <div>
    <ElCard shadow="never">
    <template #header>
      <div class="flex-y-center justify-between gap-12px">
        <span>API Definitions</span>
        <ElSpace wrap>
          <ElInput v-model="keyword" placeholder="Search code, name, URL" clearable class="w-240px" />
          <ElSelect v-model="groupFilter" placeholder="Group" clearable class="w-180px">
            <ElOption v-for="group in groups" :key="group.id" :label="group.name" :value="group.id" />
          </ElSelect>
          <ElSelect v-model="methodFilter" placeholder="Method" clearable class="w-120px">
            <ElOption v-for="method in methodOptions" :key="method" :label="method" :value="method" />
          </ElSelect>
          <ElSelect v-model="enabledFilter" placeholder="Status" clearable class="w-120px">
            <ElOption label="Enabled" :value="true" />
            <ElOption label="Disabled" :value="false" />
          </ElSelect>
          <ElButton @click="resetFilters">Reset</ElButton>
          <ElButton @click="loadData">Refresh</ElButton>
          <ElButton type="primary" @click="openCreate">New API</ElButton>
        </ElSpace>
      </div>
    </template>

    <ElAlert v-if="loadError" class="mb-12px" type="error" show-icon :closable="false" :title="loadError" />

    <ElTable
      :key="tableRenderKey"
      v-loading="loading"
      :data="filteredDefinitions"
      :empty-text="loadError ? 'Backend API is unavailable' : 'No API definitions'"
      border
    >
      <ElTableColumn prop="id" label="ID" min-width="180" show-overflow-tooltip />
      <ElTableColumn prop="apiCode" label="Code" min-width="140" />
      <ElTableColumn prop="version" label="Version" width="100" />
      <ElTableColumn prop="apiName" label="Name" min-width="160" />
      <ElTableColumn label="Group" min-width="140">
        <template #default="{ row }">{{ groupName(row.groupId) }}</template>
      </ElTableColumn>
      <ElTableColumn prop="method" label="Method" width="100" />
      <ElTableColumn prop="url" label="URL" min-width="260" show-overflow-tooltip />
      <ElTableColumn prop="bodyType" label="Body" width="130" />
      <ElTableColumn prop="timeoutMillis" label="Timeout" width="110">
        <template #default="{ row }">{{ row.timeoutMillis ? `${row.timeoutMillis} ms` : '-' }}</template>
      </ElTableColumn>
      <ElTableColumn label="Enabled" width="110">
        <template #default="{ row }">
          <ElTag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? 'Yes' : 'No' }}</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="callCount" label="Calls" width="90" />
      <ElTableColumn label="Actions" width="360" fixed="right">
        <template #default="{ row }">
          <ElButton text type="primary" @click="openDetail(row)">Detail</ElButton>
          <ElButton text type="primary" @click="openHistory(row)">History</ElButton>
          <ElButton text type="primary" :disabled="!row.enabled" @click="openRunner(row, 'simulate')">
            Simulate
          </ElButton>
          <ElButton text type="success" :disabled="!row.enabled" @click="openRunner(row, 'execute')">Execute</ElButton>
          <ElButton text type="primary" :loading="copyingId === row.id" @click="copyDefinition(row)">Copy</ElButton>
          <ElButton text type="primary" @click="openEdit(row)">Edit</ElButton>
          <ElButton text type="danger" @click="remove(row)">Delete</ElButton>
        </template>
      </ElTableColumn>
    </ElTable>
  </ElCard>

  <ElDrawer
    v-model="drawerVisible"
    :title="editingId ? 'Edit API' : 'New API'"
    size="720px"
    @closed="handleDrawerClosed"
  >
    <ElForm label-width="120px">
      <ElRow :gutter="12">
        <ElCol :span="12">
          <ElFormItem label="API Code" required>
            <ElInput v-model="form.apiCode" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="API Name" required>
            <ElInput v-model="form.apiName" />
          </ElFormItem>
        </ElCol>
      </ElRow>
      <ElFormItem label="Version">
        <ElInput v-model="form.version" :disabled="Boolean(editingId)" />
      </ElFormItem>
      <ElFormItem label="Group">
        <ElSelect v-model="form.groupId" clearable class="w-full">
          <ElOption v-for="group in groups" :key="group.id" :label="group.name" :value="group.id" />
        </ElSelect>
      </ElFormItem>
      <ElRow :gutter="12">
        <ElCol :span="8">
          <ElFormItem label="Method" required>
            <ElSelect v-model="form.method" class="w-full">
              <ElOption v-for="method in methodOptions" :key="method" :label="method" :value="method" />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="16">
          <ElFormItem label="URL" required>
            <ElInput v-model="form.url" placeholder="https://example.com/api" />
          </ElFormItem>
        </ElCol>
      </ElRow>
      <ElRow :gutter="12">
        <ElCol :span="8">
          <ElFormItem label="Body Type">
            <ElSelect v-model="form.bodyType" class="w-full">
              <ElOption v-for="item in bodyTypeOptions" :key="item" :label="item" :value="item" />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="8">
          <ElFormItem label="Timeout">
            <ElInputNumber v-model="form.timeoutMillis" :min="0" placeholder="ms" class="w-full" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="8">
          <ElFormItem label="Enabled">
            <ElSwitch v-model="form.enabled" />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElTabs>
        <ElTabPane label="Headers">
          <ElInput
            v-model="form.headersText"
            type="textarea"
            :rows="7"
            placeholder='{"Authorization":"Bearer ${secret.token}"}'
          />
        </ElTabPane>
        <ElTabPane label="Query">
          <ElInput v-model="form.queryText" type="textarea" :rows="7" placeholder='{"q":"${payload.keyword}"}' />
        </ElTabPane>
        <ElTabPane label="Body Template">
          <ElInput v-model="form.bodyTemplate" type="textarea" :rows="7" placeholder='{"name":"${payload.name}"}' />
        </ElTabPane>
        <ElTabPane label="Secret">
          <ElAlert
            class="mb-12px"
            type="info"
            show-icon
            :closable="false"
            title="Secret is encrypted by the backend. Leave empty when editing to keep the existing secret."
          />
          <ElInput v-model="form.secretText" type="textarea" :rows="7" placeholder='{"token":"secret-token"}' />
        </ElTabPane>
        <ElTabPane label="Groovy Script">
          <ElInput
            v-model="form.scriptText"
            type="textarea"
            :rows="10"
            placeholder="return [headers: [Authorization: 'Bearer ' + secret.token], query: [q: payload.keyword]]"
          />
        </ElTabPane>
      </ElTabs>
    </ElForm>
    <template #footer>
      <ElButton @click="drawerVisible = false">Cancel</ElButton>
      <ElButton type="primary" :loading="saving" @click="save">Save</ElButton>
    </template>
  </ElDrawer>

  <ElDrawer v-model="detailVisible" title="API Definition Detail" size="720px">
    <div v-loading="detailLoading">
      <ElDescriptions v-if="currentDefinition" :column="2" border>
        <ElDescriptionsItem label="ID">{{ currentDefinition.id }}</ElDescriptionsItem>
        <ElDescriptionsItem label="Tenant ID">{{ currentDefinition.tenantId }}</ElDescriptionsItem>
        <ElDescriptionsItem label="Version">{{ currentDefinition.version }}</ElDescriptionsItem>
        <ElDescriptionsItem label="Code">
          <ElSpace>
            <span>{{ currentDefinition.apiCode }}</span>
            <ElButton text type="primary" @click="copyCode(currentDefinition)">Copy</ElButton>
          </ElSpace>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="Name">{{ currentDefinition.apiName }}</ElDescriptionsItem>
        <ElDescriptionsItem label="Group">{{ groupName(currentDefinition.groupId) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="Enabled">
          <ElTag :type="currentDefinition.enabled ? 'success' : 'info'">
            {{ currentDefinition.enabled ? 'Yes' : 'No' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="Method">{{ currentDefinition.method }}</ElDescriptionsItem>
        <ElDescriptionsItem label="Body Type">{{ currentDefinition.bodyType }}</ElDescriptionsItem>
        <ElDescriptionsItem label="URL" :span="2">{{ currentDefinition.url }}</ElDescriptionsItem>
        <ElDescriptionsItem label="Call Count">{{ currentDefinition.callCount || 0 }}</ElDescriptionsItem>
        <ElDescriptionsItem label="Success / Failure">
          {{ currentDefinition.successCount || 0 }} / {{ currentDefinition.failureCount || 0 }}
        </ElDescriptionsItem>
      </ElDescriptions>

      <ElTabs v-if="currentDefinition" class="mt-16px">
        <ElTabPane label="Headers">
          <ElInput :model-value="prettyJson(currentDefinition.headersJson)" type="textarea" :rows="8" readonly />
        </ElTabPane>
        <ElTabPane label="Query">
          <ElInput :model-value="prettyJson(currentDefinition.queryJson)" type="textarea" :rows="8" readonly />
        </ElTabPane>
        <ElTabPane label="Body Template">
          <ElInput :model-value="currentDefinition.bodyTemplate || ''" type="textarea" :rows="8" readonly />
        </ElTabPane>
        <ElTabPane label="Groovy Script">
          <ElInput :model-value="currentDefinition.scriptText || ''" type="textarea" :rows="10" readonly />
        </ElTabPane>
      </ElTabs>
    </div>
  </ElDrawer>

  <ElDrawer v-model="historyVisible" title="Version History" size="860px">
    <ElTable v-loading="historyLoading" :data="versionHistory" border>
      <ElTableColumn prop="version" label="Version" width="100" />
      <ElTableColumn prop="id" label="ID" min-width="180" show-overflow-tooltip />
      <ElTableColumn prop="apiCode" label="Code" min-width="140" />
      <ElTableColumn prop="apiName" label="Name" min-width="160" />
      <ElTableColumn prop="method" label="Method" width="100" />
      <ElTableColumn prop="url" label="URL" min-width="220" show-overflow-tooltip />
      <ElTableColumn prop="createdAt" label="Created" min-width="180" />
      <ElTableColumn label="Actions" width="120" fixed="right">
        <template #default="{ row }">
          <ElButton text type="primary" @click="openDetail(row)">Detail</ElButton>
        </template>
      </ElTableColumn>
    </ElTable>
  </ElDrawer>

  <ElDrawer v-model="runnerVisible" :title="`${runMode === 'simulate' ? 'Simulate' : 'Execute'} API`" size="780px">
    <ElSpace direction="vertical" fill :size="16">
      <ElDescriptions v-if="currentDefinition" :column="2" border>
        <ElDescriptionsItem label="Code">{{ currentDefinition.apiCode }}</ElDescriptionsItem>
        <ElDescriptionsItem label="Name">{{ currentDefinition.apiName }}</ElDescriptionsItem>
        <ElDescriptionsItem label="Method">{{ currentDefinition.method }}</ElDescriptionsItem>
        <ElDescriptionsItem label="URL">{{ currentDefinition.url }}</ElDescriptionsItem>
      </ElDescriptions>

      <ElForm label-position="top">
        <ElFormItem label="Payload JSON">
          <ElInput v-model="runPayloadText" type="textarea" :rows="9" />
        </ElFormItem>
        <ElButton type="primary" :loading="running" @click="runApi">
          {{ runMode === 'simulate' ? 'Simulate' : 'Execute' }}
        </ElButton>
      </ElForm>

      <ElCard v-if="runResult" shadow="never">
        <template #header>
          <div class="flex-y-center justify-between">
            <span>Runtime Result</span>
            <ElTag :type="runResult.success ? 'success' : 'danger'">
              {{ runResult.success ? 'Success' : 'Failure' }}
            </ElTag>
          </div>
        </template>
        <ElDescriptions :column="3" border>
          <ElDescriptionsItem label="Status">{{ runResult.statusCode || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="Elapsed">{{ runResult.elapsedMillis }} ms</ElDescriptionsItem>
          <ElDescriptionsItem label="Trace">{{ runResult.traceId || '-' }}</ElDescriptionsItem>
        </ElDescriptions>
        <ElTabs class="mt-16px">
          <ElTabPane label="Response">
            <ElInput :model-value="prettyJson(runResultBody)" type="textarea" :rows="9" readonly />
          </ElTabPane>
          <ElTabPane label="Request Detail">
            <ElInput :model-value="prettyJson(runResult.requestDetail)" type="textarea" :rows="9" readonly />
          </ElTabPane>
        </ElTabs>
      </ElCard>
    </ElSpace>
    </ElDrawer>
  </div>
</template>
