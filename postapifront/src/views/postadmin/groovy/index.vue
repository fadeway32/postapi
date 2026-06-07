<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import * as monaco from 'monaco-editor';
import EditorWorker from 'monaco-editor/esm/vs/editor/editor.worker.js?worker';
import { simulateGroovy } from '@/service/api';
import { parseJsonObject, prettyJson } from '../shared';

defineOptions({ name: 'postadmin_groovy' });

type MonacoEditor = monaco.editor.IStandaloneCodeEditor;
type MonacoEnvironmentHolder = Window & typeof globalThis & { MonacoEnvironment?: monaco.Environment };

const DEFAULT_SCRIPT = `import java.time.LocalDateTime

def name = bindings.name ?: 'postadmin'
[
  message: "hello $name",
  now: LocalDateTime.now().toString(),
  doubled: [1, 2, 3].collect { it * 2 }
]`;

const DEFAULT_BINDINGS = `{
  "name": "groovy"
}`;

const DEFAULT_BLOCKED_IMPORTS = `[
  "java.io.*",
  "java.net.*",
  "java.nio.file.*",
  "java.lang.System",
  "java.lang.Runtime",
  "java.lang.ProcessBuilder"
]`;

const DEFAULT_BLOCKED_RECEIVERS = `[
  "java.io.File",
  "java.lang.System",
  "java.lang.Runtime",
  "java.lang.ProcessBuilder",
  "java.nio.file.Files"
]`;

const scriptContainer = ref<HTMLDivElement>();
const bindingsContainer = ref<HTMLDivElement>();
const resultContainer = ref<HTMLDivElement>();
const scriptText = ref(DEFAULT_SCRIPT);
const bindingsText = ref(DEFAULT_BINDINGS);
const allowedImportsText = ref('[]');
const blockedImportsText = ref(DEFAULT_BLOCKED_IMPORTS);
const blockedReceiversText = ref(DEFAULT_BLOCKED_RECEIVERS);
const timeoutMillis = ref(2000);
const installSecurityManager = ref(true);
const running = ref(false);
const result = ref<Api.PostAdmin.GroovyExecutionResult>();

let scriptEditor: MonacoEditor | null = null;
let bindingsEditor: MonacoEditor | null = null;
let resultEditor: MonacoEditor | null = null;

(window as MonacoEnvironmentHolder).MonacoEnvironment = {
  getWorker() {
    return new EditorWorker();
  }
};

function registerGroovyLanguage() {
  if (monaco.languages.getLanguages().some((item: monaco.languages.ILanguageExtensionPoint) => item.id === 'groovy')) {
    return;
  }

  monaco.languages.register({ id: 'groovy' });
  monaco.languages.setMonarchTokensProvider('groovy', {
    keywords: [
      'as',
      'assert',
      'break',
      'case',
      'catch',
      'class',
      'continue',
      'def',
      'default',
      'do',
      'else',
      'false',
      'finally',
      'for',
      'if',
      'import',
      'in',
      'new',
      'null',
      'return',
      'switch',
      'this',
      'throw',
      'true',
      'try',
      'while'
    ],
    tokenizer: {
      root: [
        [/[a-zA-Z_$][\w$]*/, { cases: { '@keywords': 'keyword', '@default': 'identifier' } }],
        [/"([^"\\]|\\.)*$/, 'string.invalid'],
        [/"/, 'string', '@string_double'],
        [/'([^'\\]|\\.)*'/, 'string'],
        [/\/\/.*$/, 'comment'],
        [/\/\*/, 'comment', '@comment'],
        [/\d+(\.\d+)?/, 'number']
      ],
      string_double: [
        [/[^\\"]+/, 'string'],
        [/\\./, 'string.escape'],
        [/"/, 'string', '@pop']
      ],
      comment: [
        [/[^\/*]+/, 'comment'],
        [/\*\//, 'comment', '@pop'],
        [/[\/*]/, 'comment']
      ]
    }
  });
}

function createEditor(
  element: HTMLDivElement,
  value: string,
  language: string,
  onChange: (value: string) => void,
  readOnly = false
) {
  const editor = monaco.editor.create(element, {
    value,
    language,
    readOnly,
    automaticLayout: true,
    minimap: { enabled: false },
    fontSize: 13,
    lineHeight: 20,
    scrollBeyondLastLine: false,
    tabSize: 2,
    wordWrap: 'on'
  });
  editor.onDidChangeModelContent(() => onChange(editor.getValue()));
  return editor;
}

function parseJsonArray(text: string, field: string) {
  const trimmed = text.trim();
  if (!trimmed) {
    return [];
  }
  const parsed = JSON.parse(trimmed);
  if (!Array.isArray(parsed) || parsed.some(item => typeof item !== 'string')) {
    throw new Error(`${field} must be a string array`);
  }
  return parsed as string[];
}

const resultText = computed(() => prettyJson(result.value || {}));

watch(resultText, value => {
  resultEditor?.setValue(value);
});

async function runGroovy() {
  let bindings: Record<string, unknown>;
  let allowedImports: string[];
  let blockedImports: string[];
  let blockedReceivers: string[];

  try {
    bindings = parseJsonObject(bindingsText.value);
    allowedImports = parseJsonArray(allowedImportsText.value, 'allowedImports');
    blockedImports = parseJsonArray(blockedImportsText.value, 'blockedImports');
    blockedReceivers = parseJsonArray(blockedReceiversText.value, 'blockedReceivers');
  } catch (error) {
    window.$message?.error((error as Error).message);
    return;
  }

  running.value = true;
  const { data, error } = await simulateGroovy({
    script: scriptText.value,
    bindings,
    timeoutMillis: timeoutMillis.value,
    allowedImports,
    blockedImports,
    blockedReceivers,
    installSecurityManager: installSecurityManager.value
  });
  running.value = false;

  if (!error) {
    result.value = data;
  }
}

function resetAll() {
  scriptText.value = DEFAULT_SCRIPT;
  bindingsText.value = DEFAULT_BINDINGS;
  allowedImportsText.value = '[]';
  blockedImportsText.value = DEFAULT_BLOCKED_IMPORTS;
  blockedReceiversText.value = DEFAULT_BLOCKED_RECEIVERS;
  scriptEditor?.setValue(scriptText.value);
  bindingsEditor?.setValue(bindingsText.value);
  result.value = undefined;
}

onMounted(async () => {
  registerGroovyLanguage();
  await nextTick();
  if (scriptContainer.value) {
    scriptEditor = createEditor(scriptContainer.value, scriptText.value, 'groovy', value => {
      scriptText.value = value;
    });
  }
  if (bindingsContainer.value) {
    bindingsEditor = createEditor(bindingsContainer.value, bindingsText.value, 'json', value => {
      bindingsText.value = value;
    });
  }
  if (resultContainer.value) {
    resultEditor = createEditor(resultContainer.value, resultText.value, 'json', () => {}, true);
  }
});

onBeforeUnmount(() => {
  scriptEditor?.dispose();
  bindingsEditor?.dispose();
  resultEditor?.dispose();
});
</script>

<template>
  <div class="groovy-page">
    <ElRow :gutter="16">
      <ElCol :xs="24" :xl="14">
        <ElCard header="Groovy Script" shadow="never">
          <div ref="scriptContainer" class="editor editor-script"></div>
          <div class="mt-16px flex flex-wrap items-center gap-12px">
            <ElInputNumber v-model="timeoutMillis" :min="100" :max="10000" :step="100" controls-position="right" />
            <ElCheckbox v-model="installSecurityManager">SecurityManager</ElCheckbox>
            <ElButton type="primary" :loading="running" @click="runGroovy">
              <template #icon>
                <SvgIcon icon="carbon:play" />
              </template>
              Run
            </ElButton>
            <ElButton @click="resetAll">
              <template #icon>
                <SvgIcon icon="carbon:reset" />
              </template>
              Reset
            </ElButton>
          </div>
        </ElCard>

        <ElCard header="Bindings JSON" shadow="never" class="mt-16px">
          <div ref="bindingsContainer" class="editor editor-json"></div>
        </ElCard>
      </ElCol>

      <ElCol :xs="24" :xl="10">
        <ElCard header="Result" shadow="never">
          <ElDescriptions v-if="result" :column="2" border>
            <ElDescriptionsItem label="Success">
              <ElTag :type="result.success ? 'success' : 'danger'">{{ result.success ? 'Yes' : 'No' }}</ElTag>
            </ElDescriptionsItem>
            <ElDescriptionsItem label="Elapsed">{{ result.elapsedMillis }} ms</ElDescriptionsItem>
            <ElDescriptionsItem label="Timeout">
              <ElTag :type="result.timeout ? 'warning' : 'info'">{{ result.timeout ? 'Yes' : 'No' }}</ElTag>
            </ElDescriptionsItem>
            <ElDescriptionsItem label="SecurityManager">
              <ElTag :type="result.securityManagerActive ? 'success' : 'warning'">
                {{ result.securityManagerActive ? 'Active' : 'Fallback' }}
              </ElTag>
            </ElDescriptionsItem>
            <ElDescriptionsItem v-if="result.errorType" label="Error">{{ result.errorType }}</ElDescriptionsItem>
            <ElDescriptionsItem v-if="result.securityManagerMessage" label="Security">
              {{ result.securityManagerMessage }}
            </ElDescriptionsItem>
          </ElDescriptions>
          <div ref="resultContainer" class="editor editor-result mt-16px"></div>
        </ElCard>

        <ElCard header="Policy JSON" shadow="never" class="mt-16px">
          <ElTabs>
            <ElTabPane label="Allowed Imports">
              <ElInput v-model="allowedImportsText" type="textarea" :rows="6" />
            </ElTabPane>
            <ElTabPane label="Blocked Imports">
              <ElInput v-model="blockedImportsText" type="textarea" :rows="6" />
            </ElTabPane>
            <ElTabPane label="Blocked Receivers">
              <ElInput v-model="blockedReceiversText" type="textarea" :rows="6" />
            </ElTabPane>
          </ElTabs>
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>

<style scoped>
.groovy-page {
  min-height: 100%;
}

.editor {
  overflow: hidden;
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
}

.editor-script {
  height: 460px;
}

.editor-json {
  height: 220px;
}

.editor-result {
  height: 420px;
}
</style>
