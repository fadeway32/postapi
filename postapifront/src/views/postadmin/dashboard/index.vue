<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { fetchApiDefinitions, fetchApiGroups, fetchApiLogs, fetchGroupStats, fetchTopApiStats } from '@/service/api';

defineOptions({ name: 'postadmin_dashboard' });

const loading = ref(false);
const apiCount = ref(0);
const groupCount = ref(0);
const logCount = ref(0);
const topApis = ref<Api.PostAdmin.TopApiStat[]>([]);
const groupStats = ref<Api.PostAdmin.GroupStat[]>([]);

async function loadData() {
  loading.value = true;
  const [definitions, groups, logs, top, stats] = await Promise.all([
    fetchApiDefinitions(),
    fetchApiGroups(),
    fetchApiLogs({ page: 1, size: 1 }),
    fetchTopApiStats(8),
    fetchGroupStats()
  ]);

  if (!definitions.error) apiCount.value = definitions.data.length;
  if (!groups.error) groupCount.value = groups.data.length;
  if (!logs.error) logCount.value = logs.data.total;
  if (!top.error) topApis.value = top.data;
  if (!stats.error) groupStats.value = stats.data;
  loading.value = false;
}

onMounted(loadData);
</script>

<template>
  <ElSpace direction="vertical" fill :size="16" v-loading="loading">
    <ElRow :gutter="16">
      <ElCol :xs="24" :md="8">
        <ElCard shadow="never">
          <div class="metric-label">API Definitions</div>
          <div class="metric-value">{{ apiCount }}</div>
        </ElCard>
      </ElCol>
      <ElCol :xs="24" :md="8">
        <ElCard shadow="never">
          <div class="metric-label">Groups</div>
          <div class="metric-value">{{ groupCount }}</div>
        </ElCard>
      </ElCol>
      <ElCol :xs="24" :md="8">
        <ElCard shadow="never">
          <div class="metric-label">Call Logs</div>
          <div class="metric-value">{{ logCount }}</div>
        </ElCard>
      </ElCol>
    </ElRow>

    <ElRow :gutter="16">
      <ElCol :xs="24" :lg="14">
        <ElCard header="Top APIs" shadow="never">
          <ElTable :data="topApis" border>
            <ElTableColumn prop="apiCode" label="Code" min-width="140" />
            <ElTableColumn prop="apiName" label="Name" min-width="180" />
            <ElTableColumn prop="callCount" label="Calls" width="90" />
            <ElTableColumn prop="successCount" label="Success" width="90" />
            <ElTableColumn prop="failureCount" label="Failure" width="90" />
          </ElTable>
        </ElCard>
      </ElCol>
      <ElCol :xs="24" :lg="10">
        <ElCard header="Group Stats" shadow="never">
          <ElTable :data="groupStats" border>
            <ElTableColumn prop="groupName" label="Group" min-width="160" />
            <ElTableColumn prop="callCount" label="Calls" width="90" />
            <ElTableColumn prop="successCount" label="OK" width="80" />
            <ElTableColumn prop="failureCount" label="Fail" width="80" />
          </ElTable>
        </ElCard>
      </ElCol>
    </ElRow>
  </ElSpace>
</template>

<style scoped>
.metric-label {
  color: #667085;
  font-size: 13px;
}

.metric-value {
  margin-top: 8px;
  color: #101828;
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}
</style>
