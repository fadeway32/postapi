<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { createTenant, deleteTenant, fetchTenants, updateTenant } from '@/service/api';

defineOptions({ name: 'postadmin_tenants' });

const loading = ref(false);
const saving = ref(false);
const drawerVisible = ref(false);
const tenants = ref<Api.PostAdmin.Tenant[]>([]);
const editingId = ref<Api.PostAdmin.Id>();
const needRefreshAfterDrawerClose = ref(false);

const form = reactive<Api.PostAdmin.TenantSaveRequest>({
  code: '',
  name: '',
  enabled: true
});

async function loadData() {
  loading.value = true;
  const { data, error } = await fetchTenants();
  if (!error) tenants.value = data;
  loading.value = false;
}

function upsertTenant(tenant: Api.PostAdmin.Tenant) {
  const index = tenants.value.findIndex(item => item.id === tenant.id);
  if (index >= 0) {
    tenants.value.splice(index, 1, tenant);
    return;
  }

  tenants.value.unshift(tenant);
}

function resetForm() {
  editingId.value = undefined;
  Object.assign(form, { code: '', name: '', enabled: true });
}

function openCreate() {
  resetForm();
  drawerVisible.value = true;
}

function openEdit(row: Api.PostAdmin.Tenant) {
  editingId.value = row.id;
  Object.assign(form, { code: row.code, name: row.name, enabled: row.enabled });
  drawerVisible.value = true;
}

async function save() {
  if (!form.code.trim() || !form.name.trim()) {
    window.$message?.warning('Code and name are required');
    return;
  }

  saving.value = true;
  const result = editingId.value ? await updateTenant(editingId.value, form) : await createTenant(form);
  saving.value = false;

  if (!result.error) {
    if (result.data) {
      upsertTenant(result.data);
    }

    needRefreshAfterDrawerClose.value = true;
    drawerVisible.value = false;
    window.$message?.success('Saved');
  }
}

async function handleDrawerClosed() {
  resetForm();

  if (!needRefreshAfterDrawerClose.value) {
    return;
  }

  needRefreshAfterDrawerClose.value = false;
  await loadData();
}

async function remove(row: Api.PostAdmin.Tenant) {
  await window.$messageBox?.confirm(`Delete tenant "${row.code}"?`, 'Confirm', { type: 'warning' });
  const { error } = await deleteTenant(row.id);
  if (!error) {
    window.$message?.success('Deleted');
    await loadData();
  }
}

onMounted(loadData);
</script>

<template>
  <div>
    <ElCard shadow="never">
    <template #header>
      <div class="flex-y-center justify-between">
        <span>Tenants</span>
        <ElSpace>
          <ElButton @click="loadData">Refresh</ElButton>
          <ElButton type="primary" @click="openCreate">New Tenant</ElButton>
        </ElSpace>
      </div>
    </template>

    <ElTable v-loading="loading" :data="tenants" border>
      <ElTableColumn prop="code" label="Code" min-width="160" />
      <ElTableColumn prop="name" label="Name" min-width="180" />
      <ElTableColumn label="Enabled" width="110">
        <template #default="{ row }">
          <ElTag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? 'Yes' : 'No' }}</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="createdAt" label="Created" min-width="180" />
      <ElTableColumn label="Actions" width="180" fixed="right">
        <template #default="{ row }">
          <ElButton text type="primary" @click="openEdit(row)">Edit</ElButton>
          <ElButton text type="danger" @click="remove(row)">Delete</ElButton>
        </template>
      </ElTableColumn>
    </ElTable>
  </ElCard>

  <ElDrawer v-model="drawerVisible" :title="editingId ? 'Edit Tenant' : 'New Tenant'" size="420px" @closed="handleDrawerClosed">
    <ElForm label-width="96px">
      <ElFormItem label="Code" required>
        <ElInput v-model="form.code" />
      </ElFormItem>
      <ElFormItem label="Name" required>
        <ElInput v-model="form.name" />
      </ElFormItem>
      <ElFormItem label="Enabled">
        <ElSwitch v-model="form.enabled" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="drawerVisible = false">Cancel</ElButton>
      <ElButton type="primary" :loading="saving" @click="save">Save</ElButton>
    </template>
    </ElDrawer>
  </div>
</template>
