<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { batchExecute, executeApi, fetchApiDefinitions, simulateApi } from '@/service/api';
import { parseJsonObject, prettyJson } from '../shared';

defineOptions({ name: 'postadmin_runtime' });

const loading = ref(false);
const running = ref(false);
const definitions = ref<Api.PostAdmin.ApiDefinition[]>([]);
const apiCode = ref('');
const mode = ref<'simulate' | 'execute'>('simulate');
const payloadText = ref('{\n  "keyword": "demo"\n}');
const result = ref<Api.PostAdmin.ApiExecutionResult>();
const batchText = ref('[\n  {\n    "apiCode": "",\n    "payload": {}\n  }\n]');
const batchResult = ref<Api.PostAdmin.BatchExecutionResult>();
const stopOnFailure = ref(false);

const enabledApis = computed(() => definitions.value.filter(item => item.enabled));

async function loadDefinitions() {
  loading.value = true;
  const { data, error } = await fetchApiDefinitions();
  if (!error) {
    definitions.value = data;
    if (!apiCode.value && data.length) {
      apiCode.value = data[0].apiCode;
      batchText.value = prettyJson([{ apiCode: data[0].apiCode, payload: {} }]);
    }
  }
  loading.value = false;
}

async function runSingle() {
  if (!apiCode.value) {
    window.$message?.warning('Please select an API');
    return;
  }

  let payload: Record<string, unknown>;
  try {
    payload = parseJsonObject(payloadText.value);
  } catch (error) {
    window.$message?.error((error as Error).message);
    return;
  }

  running.value = true;
  const request = mode.value === 'simulate' ? simulateApi(apiCode.value, payload) : executeApi(apiCode.value, payload);
  const { data, error } = await request;
  running.value = false;

  if (!error) {
    result.value = data;
  }
}

async function runBatch() {
  let items: Api.PostAdmin.BatchExecuteRequest['items'];
  try {
    const parsed = JSON.parse(batchText.value);
    if (!Array.isArray(parsed)) throw new Error('Batch JSON must be an array');
    items = parsed;
  } catch (error) {
    window.$message?.error((error as Error).message);
    return;
  }

  running.value = true;
  const { data, error } = await batchExecute({ items, stopOnFailure: stopOnFailure.value });
  running.value = false;

  if (!error) {
    batchResult.value = data;
  }
}

onMounted(loadDefinitions);
</script>

<template>
  <div>
    <ElRow :gutter="16">
    <ElCol :xs="24" :lg="11">
      <ElCard header="Single Run" shadow="never" v-loading="loading">
        <ElForm label-position="top">
          <ElFormItem label="API">
            <ElSelect v-model="apiCode" filterable class="w-full">
              <ElOption
                v-for="api in enabledApis"
                :key="api.apiCode"
                :label="`${api.apiName} (${api.apiCode})`"
                :value="api.apiCode"
              />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="Mode">
            <ElSegmented v-model="mode" :options="['simulate', 'execute']" />
          </ElFormItem>
          <ElFormItem label="Payload JSON">
            <ElInput v-model="payloadText" type="textarea" :rows="12" />
          </ElFormItem>
          <ElButton type="primary" :loading="running" @click="runSingle">Run</ElButton>
        </ElForm>
      </ElCard>
    </ElCol>

    <ElCol :xs="24" :lg="13">
      <ElCard header="Result" shadow="never">
        <ElDescriptions v-if="result" :column="2" border>
          <ElDescriptionsItem label="Success">
            <ElTag :type="result.success ? 'success' : 'danger'">{{ result.success ? 'Yes' : 'No' }}</ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="Status">{{ result.statusCode || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="Elapsed">{{ result.elapsedMillis }} ms</ElDescriptionsItem>
          <ElDescriptionsItem label="Trace">{{ result.traceId || '-' }}</ElDescriptionsItem>
        </ElDescriptions>
        <ElTabs class="mt-16px">
          <ElTabPane label="Response">
            <ElInput :model-value="prettyJson(result?.responseBody || result?.errorMessage || '')" type="textarea" :rows="10" readonly />
          </ElTabPane>
          <ElTabPane label="Request Detail">
            <ElInput :model-value="prettyJson(result?.requestDetail)" type="textarea" :rows="10" readonly />
          </ElTabPane>
        </ElTabs>
      </ElCard>
    </ElCol>
  </ElRow>

  <ElCard header="Batch Run" shadow="never" class="mt-16px">
    <ElRow :gutter="16">
      <ElCol :xs="24" :lg="11">
        <ElForm label-position="top">
          <ElFormItem label="Batch Items JSON">
            <ElInput v-model="batchText" type="textarea" :rows="12" />
          </ElFormItem>
          <ElFormItem>
            <ElCheckbox v-model="stopOnFailure">Stop on failure</ElCheckbox>
          </ElFormItem>
          <ElButton type="primary" :loading="running" @click="runBatch">Run Batch</ElButton>
        </ElForm>
      </ElCol>
      <ElCol :xs="24" :lg="13">
        <ElDescriptions v-if="batchResult" :column="4" border>
          <ElDescriptionsItem label="Batch">{{ batchResult.batchId }}</ElDescriptionsItem>
          <ElDescriptionsItem label="Total">{{ batchResult.total }}</ElDescriptionsItem>
          <ElDescriptionsItem label="Success">{{ batchResult.success }}</ElDescriptionsItem>
          <ElDescriptionsItem label="Failure">{{ batchResult.failure }}</ElDescriptionsItem>
        </ElDescriptions>
        <ElTable class="mt-16px" :data="batchResult?.results || []" border>
          <ElTableColumn prop="apiCode" label="API" min-width="140" />
          <ElTableColumn label="Success" width="100">
            <template #default="{ row }">
              <ElTag :type="row.success ? 'success' : 'danger'">{{ row.success ? 'Yes' : 'No' }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="statusCode" label="Status" width="100" />
          <ElTableColumn prop="elapsedMillis" label="Elapsed" width="110" />
          <ElTableColumn prop="errorMessage" label="Error" min-width="180" show-overflow-tooltip />
        </ElTable>
      </ElCol>
    </ElRow>
    </ElCard>
  </div>
</template>
