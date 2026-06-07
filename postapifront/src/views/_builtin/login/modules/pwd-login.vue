<script setup lang="ts">
import { computed, ref } from 'vue';
import { loginModuleRecord } from '@/constants/app';
import { useAuthStore } from '@/store/modules/auth';
import { useRouterPush } from '@/hooks/common/router';
import { useForm, useFormRules } from '@/hooks/common/form';
import { $t } from '@/locales';

defineOptions({ name: 'PwdLogin' });

const authStore = useAuthStore();
const { toggleLoginModule } = useRouterPush();
const { formRef, validate } = useForm();

interface FormModel {
  tenantCode: string;
  userName: string;
  password: string;
}

const model = ref<FormModel>({
  tenantCode: 'demo',
  userName: 'admin',
  password: 'admin123'
});

const rules = computed<Record<keyof FormModel, App.Global.FormRule[]>>(() => {
  // inside computed to make locale ref, if not apply i18n, you can define it without computed
  const { formRules } = useFormRules();

  return {
    tenantCode: [{ required: true, message: 'Please input tenant code', trigger: 'blur' }],
    userName: formRules.userName,
    password: formRules.pwd
  };
});

async function handleSubmit() {
  await validate();
  await authStore.login(model.value.userName, model.value.password, {
    redirect: true,
    tenantCode: model.value.tenantCode
  });
}

type AccountKey = 'super' | 'admin' | 'user';

interface Account {
  key: AccountKey;
  label: string;
  tenantCode: string;
  userName: string;
  password: string;
}

const accounts = computed<Account[]>(() => [
  {
    key: 'super',
    label: 'Demo Admin',
    tenantCode: 'demo',
    userName: 'admin',
    password: 'admin123'
  },
  {
    key: 'admin',
    label: 'Fill Demo',
    tenantCode: 'demo',
    userName: 'admin',
    password: 'admin123'
  },
  {
    key: 'user',
    label: 'Local',
    tenantCode: 'demo',
    userName: 'admin',
    password: 'admin123'
  }
]);

async function handleAccountLogin(account: Account) {
  model.value = {
    tenantCode: account.tenantCode,
    userName: account.userName,
    password: account.password
  };
  await authStore.login(account.userName, account.password, {
    redirect: true,
    tenantCode: account.tenantCode
  });
}
</script>

<template>
  <ElForm ref="formRef" :model="model" :rules="rules" size="large" :show-label="false" @keyup.enter="handleSubmit">
    <ElFormItem prop="tenantCode">
      <ElInput v-model="model.tenantCode" placeholder="Tenant code" />
    </ElFormItem>
    <ElFormItem prop="userName">
      <ElInput v-model="model.userName" :placeholder="$t('page.login.common.userNamePlaceholder')" />
    </ElFormItem>
    <ElFormItem prop="password">
      <ElInput
        v-model="model.password"
        type="password"
        show-password-on="click"
        :placeholder="$t('page.login.common.passwordPlaceholder')"
      />
    </ElFormItem>
    <ElSpace direction="vertical" :size="24" class="w-full" fill>
      <div class="flex-y-center justify-between">
        <ElCheckbox>{{ $t('page.login.pwdLogin.rememberMe') }}</ElCheckbox>
        <span class="text-13px text-#999">postadmin-token</span>
      </div>
      <ElButton type="primary" size="large" round block :loading="authStore.loginLoading" @click="handleSubmit">
        {{ $t('common.confirm') }}
      </ElButton>
      <div class="flex-y-center justify-between gap-12px">
        <ElButton class="flex-1" size="default" @click="toggleLoginModule('code-login')">
          {{ $t(loginModuleRecord['code-login']) }}
        </ElButton>
        <ElButton class="flex-1" size="default" @click="toggleLoginModule('register')">
          {{ $t(loginModuleRecord.register) }}
        </ElButton>
      </div>
      <ElDivider class="text-14px text-#666 !m-0">{{ $t('page.login.pwdLogin.otherAccountLogin') }}</ElDivider>
      <div class="flex-center gap-12px">
        <ElButton
          v-for="item in accounts"
          :key="item.key"
          size="default"
          type="primary"
          :disabled="authStore.loginLoading"
          @click="handleAccountLogin(item)"
        >
          {{ item.label }}
        </ElButton>
      </div>
    </ElSpace>
  </ElForm>
</template>

<style scoped></style>
