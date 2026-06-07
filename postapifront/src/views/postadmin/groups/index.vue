<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { createApiGroup, deleteApiGroup, fetchApiGroups, updateApiGroup } from '@/service/api';

defineOptions({ name: 'postadmin_groups' });

const loading = ref(false);
const saving = ref(false);
const drawerVisible = ref(false);
const groups = ref<Api.PostAdmin.ApiGroup[]>([]);
const editingId = ref<Api.PostAdmin.Id>();
const needRefreshAfterDrawerClose = ref(false);

const form = reactive<Api.PostAdmin.ApiGroupSaveRequest>({
  name: '',
  sortOrder: 0
});

async function loadData() {
  loading.value = true;
  const { data, error } = await fetchApiGroups();
  if (!error) groups.value = data;
  loading.value = false;
}

function upsertGroup(group: Api.PostAdmin.ApiGroup) {
  const index = groups.value.findIndex(item => item.id === group.id);
  if (index >= 0) {
    groups.value.splice(index, 1, group);
    return;
  }

  groups.value.unshift(group);
}

function resetForm() {
  editingId.value = undefined;
  form.name = '';
  form.sortOrder = 0;
}

function openCreate() {
  resetForm();
  drawerVisible.value = true;
}

function openEdit(row: Api.PostAdmin.ApiGroup) {
  editingId.value = row.id;
  form.name = row.name;
  form.sortOrder = row.sortOrder || 0;
  drawerVisible.value = true;
}

async function save() {
  if (!form.name.trim()) {
    window.$message?.warning('Name is required');
    return;
  }

  saving.value = true;
  const result = editingId.value ? await updateApiGroup(editingId.value, form) : await createApiGroup(form);
  saving.value = false;

  if (!result.error) {
    if (result.data) {
      upsertGroup(result.data);
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

async function remove(row: Api.PostAdmin.ApiGroup) {
  await window.$messageBox?.confirm(`Delete group "${row.name}"?`, 'Confirm', { type: 'warning' });
  const { error } = await deleteApiGroup(row.id);
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
        <span>API Groups</span>
        <ElSpace>
          <ElButton @click="loadData">Refresh</ElButton>
          <ElButton type="primary" @click="openCreate">New Group</ElButton>
        </ElSpace>
      </div>
    </template>

    <ElTable v-loading="loading" :data="groups" border>
      <ElTableColumn prop="name" label="Name" min-width="180" />
      <ElTableColumn prop="sortOrder" label="Sort" width="100" />
      <ElTableColumn prop="createdAt" label="Created" min-width="180" />
      <ElTableColumn prop="updatedAt" label="Updated" min-width="180" />
      <ElTableColumn label="Actions" width="180" fixed="right">
        <template #default="{ row }">
          <ElButton text type="primary" @click="openEdit(row)">Edit</ElButton>
          <ElButton text type="danger" @click="remove(row)">Delete</ElButton>
        </template>
      </ElTableColumn>
    </ElTable>
  </ElCard>

  <ElDrawer v-model="drawerVisible" :title="editingId ? 'Edit Group' : 'New Group'" size="420px" @closed="handleDrawerClosed">
    <ElForm label-width="96px">
      <ElFormItem label="Name" required>
        <ElInput v-model="form.name" />
      </ElFormItem>
      <ElFormItem label="Sort">
        <ElInputNumber v-model="form.sortOrder" :min="0" class="w-full" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="drawerVisible = false">Cancel</ElButton>
      <ElButton type="primary" :loading="saving" @click="save">Save</ElButton>
    </template>
    </ElDrawer>
  </div>
</template>
