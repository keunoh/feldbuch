<script setup>
import {computed, nextTick, ref, watch,} from "vue";

import DOMPurify from "dompurify";
import {marked} from "marked";

import hljs from "highlight.js/lib/core";
import bash from "highlight.js/lib/languages/bash";
import java from "highlight.js/lib/languages/java";
import javascript from "highlight.js/lib/languages/javascript";
import json from "highlight.js/lib/languages/json";
import sql from "highlight.js/lib/languages/sql";
import xml from "highlight.js/lib/languages/xml";
import yaml from "highlight.js/lib/languages/yaml";

import "highlight.js/styles/github-dark.css";

import {getKnowledgeNote,} from "@/api/knowledgeApi.js";

hljs.registerLanguage(
  "java",
  java,
);

hljs.registerLanguage(
  "javascript",
  javascript,
);

hljs.registerLanguage(
  "js",
  javascript,
);

hljs.registerLanguage(
  "vue",
  xml,
);

hljs.registerLanguage(
  "html",
  xml,
);

hljs.registerLanguage(
  "xml",
  xml,
);

hljs.registerLanguage(
  "sql",
  sql,
);

hljs.registerLanguage(
  "yaml",
  yaml,
);

hljs.registerLanguage(
  "yml",
  yaml,
);

hljs.registerLanguage(
  "bash",
  bash,
);

hljs.registerLanguage(
  "shell",
  bash,
);

hljs.registerLanguage(
  "json",
  json,
);

const props = defineProps({
  noteId: {
    type: Number,
    required: true,
  },
});

const note = ref(null);
const loading = ref(false);
const errorMessage = ref("");

marked.setOptions({
  gfm: true,
  breaks: true,
});

const renderedSummary = computed(() => {
  const summary =
    note.value?.summary;

  if (
    !summary
    || typeof summary !== "string"
  ) {
    return "";
  }

  const html =
    marked.parse(
      summary,
    );

  return DOMPurify.sanitize(
    html,
    {
      ADD_ATTR: [
        "class",
      ],
    },
  );
});

async function loadNote(
  noteId,
) {
  loading.value = true;
  errorMessage.value = "";
  note.value = null;

  try {
    const response =
      await getKnowledgeNote(
        noteId,
      );

    note.value =
      response.data;

    await nextTick();

    highlightCodeBlocks();
  } catch (error) {
    console.error(
      "Knowledge 노트 상세 조회 실패",
      error,
    );

    note.value = null;
    errorMessage.value =
      "지식 노트를 불러오지 못했습니다.";
  } finally {
    loading.value = false;
  }
}

function highlightCodeBlocks() {
  const blocks =
    document.querySelectorAll(
      ".knowledge-note-detail pre code",
    );

  blocks.forEach(block => {
    if (
      block.dataset.highlighted
    ) {
      return;
    }

    hljs.highlightElement(
      block,
    );

    addLanguageLabel(
      block,
    );
  });
}

function addLanguageLabel(
  codeElement,
) {
  const preElement =
    codeElement.parentElement;

  if (
    !preElement
    || preElement.querySelector(
      ".code-language-label",
    )
  ) {
    return;
  }

  const language =
    findLanguageName(
      codeElement,
    );

  if (!language) {
    return;
  }

  const label =
    document.createElement(
      "span",
    );

  label.className =
    "code-language-label";

  label.textContent =
    language.toUpperCase();

  preElement.appendChild(
    label,
  );
}

function findLanguageName(
  codeElement,
) {
  const languageClass =
    Array.from(
      codeElement.classList,
    )
      .find(className =>
        className.startsWith(
          "language-",
        ),
      );

  if (languageClass) {
    return languageClass.replace(
      "language-",
      "",
    );
  }

  const detectedClass =
    Array.from(
      codeElement.classList,
    )
      .find(className =>
        className.startsWith(
          "hljs-",
        ),
      );

  return detectedClass
    ?.replace(
      "hljs-",
      "",
    );
}

watch(
  () => props.noteId,
  async noteId => {
    if (!noteId) {
      note.value = null;
      return;
    }

    await loadNote(
      noteId,
    );
  },
  {
    immediate: true,
  },
);

watch(
  renderedSummary,
  async () => {
    await nextTick();

    highlightCodeBlocks();
  },
);
</script>

<template>
  <article class="knowledge-note-detail">
    <div
      v-if="loading"
      class="detail-state"
    >
      노트를 불러오는 중...
    </div>

    <div
      v-else-if="errorMessage"
      class="detail-state error"
    >
      {{ errorMessage }}
    </div>

    <div
      v-else-if="note"
      class="note-content"
    >
      <header class="note-header">
        <p class="note-eyebrow">
          KNOWLEDGE NOTE
        </p>

        <h2>
          {{ note.title }}
        </h2>

        <p class="note-description">
          {{ note.description }}
        </p>
      </header>

      <section class="note-section">
        <h3 class="section-label">
          CONTENT
        </h3>

        <div
          class="note-summary markdown-content"
          v-html="renderedSummary"
        />
      </section>

      <section
        v-if="note.keywords?.length"
        class="note-section"
      >
        <h3 class="section-label">
          KEYWORDS
        </h3>

        <div class="keyword-list">
          <span
            v-for="keyword in note.keywords"
            :key="keyword"
            class="keyword"
          >
            #{{ keyword }}
          </span>
        </div>
      </section>
    </div>
  </article>
</template>

<style scoped>
.knowledge-note-detail {
  min-height: 100%;
  padding: 28px;
  box-sizing: border-box;
}

.detail-state {
  display: grid;
  min-height: 280px;
  place-items: center;
  color: var(--color-text-muted);
  font-size: 14px;
}

.detail-state.error {
  color: var(--color-danger);
}

.note-content {
  max-width: 860px;
  margin: 0 auto;
}

.note-header {
  padding-bottom: var(--space-10);
  border-bottom: 1px solid var(--color-border-soft);
}

.note-eyebrow {
  margin: 0 0 var(--space-3);
  color: var(--color-primary);
  font-family: var(--font-family-terminal);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.note-header h2 {
  margin: 0;
  color: var(--color-text);
  font-size: 28px;
  line-height: 1.35;
}

.note-description {
  margin: 14px 0 0;
  color: var(--color-text-soft);
  font-size: 14px;
  line-height: 1.7;
}

.note-section {
  padding: var(--space-10) 0;
  border-bottom: 1px solid var(--color-border-soft);
}

.section-label {
  margin: 0 0 var(--space-8);
  color: var(--color-primary);
  font-family: var(--font-family-terminal);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.1em;
}

.note-summary {
  color: var(--color-text-soft);
  font-size: 15px;
  line-height: 1.85;
}

.keyword-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.keyword {
  padding: 6px var(--space-4);
  border: 1px solid var(--color-border-primary);
  border-radius: var(--radius-round);
  color: var(--color-primary);
  background: var(--color-primary-soft);
  font-size: 12px;
}

.markdown-content :deep(h2) {
  margin: 34px 0 14px;
  padding-bottom: 9px;
  border-bottom: 1px solid var(--color-border-soft);
  color: var(--color-text);
  font-size: 21px;
  line-height: 1.45;
}

.markdown-content :deep(h2:first-child) {
  margin-top: 0;
}

.markdown-content :deep(h3) {
  margin: 28px 0 var(--space-5);
  color: var(--color-text);
  font-size: 17px;
  line-height: 1.5;
}

.markdown-content :deep(h4) {
  margin: 22px 0 var(--space-4);
  color: var(--color-text);
  font-size: 15px;
}

.markdown-content :deep(p) {
  margin: 0 0 var(--space-7);
  color: var(--color-text-soft);
  line-height: 1.85;
}

.markdown-content :deep(strong) {
  color: var(--color-text);
  font-weight: 700;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: var(--space-4) 0 var(--space-8);
  padding-left: 26px;
}

.markdown-content :deep(li) {
  margin: 7px 0;
  color: var(--color-text-soft);
  line-height: 1.75;
}

.markdown-content :deep(li::marker) {
  color: var(--color-primary);
}

.markdown-content :deep(blockquote) {
  margin: var(--space-9) 0;
  padding: var(--space-5) var(--space-7);
  border-left: 3px solid var(--color-primary);
  color: var(--color-text-soft);
  background: var(--color-primary-soft);
}

.markdown-content :deep(code) {
  padding: 2px 6px;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-5);
  color: var(--color-primary);
  background: var(--color-surface-raised);
  font-family: var(--font-family-terminal);
  font-size: 0.88em;
}

.markdown-content :deep(pre) {
  position: relative;
  margin: var(--space-8) 0 22px;
  overflow-x: auto;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-10);
  background: var(--color-knowledge-code-bg);
}

.markdown-content :deep(pre code) {
  display: block;
  min-width: max-content;
  padding: 42px var(--space-8) var(--space-8);
  border: 0;
  color: var(--color-knowledge-code-text);
  background: transparent;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre;
}

.markdown-content :deep(.code-language-label) {
  position: absolute;
  top: 10px;
  right: 12px;
  z-index: 1;
  padding: 3px 7px;
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-5);
  color: var(--color-text-muted);
  background: var(--color-white-a040);
  font-family: var(--font-family-terminal);
  font-size: 9px;
  letter-spacing: 0.08em;
}

.markdown-content :deep(table) {
  width: 100%;
  margin: var(--space-9) 0;
  border-collapse: collapse;
  border: 1px solid var(--color-border-soft);
  font-size: 13px;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  padding: 11px 13px;
  border: 1px solid var(--color-border-soft);
  text-align: left;
  vertical-align: top;
}

.markdown-content :deep(th) {
  color: var(--color-text);
  background: var(--color-surface-raised);
  font-weight: 700;
}

.markdown-content :deep(td) {
  color: var(--color-text-soft);
}

.markdown-content :deep(a) {
  color: var(--color-primary);
  text-decoration: underline;
  text-underline-offset: 3px;
}

.markdown-content :deep(hr) {
  margin: 30px 0;
  border: 0;
  border-top: 1px solid var(--color-border-soft);
}

.markdown-content :deep(img) {
  display: block;
  max-width: 100%;
  height: auto;
  margin: var(--space-9) auto;
  border-radius: var(--radius-8);
}

@media (max-width: 700px) {
  .knowledge-note-detail {
    padding: var(--space-9);
  }

  .note-header h2 {
    font-size: 23px;
  }

  .markdown-content :deep(h2) {
    font-size: 19px;
  }

  .markdown-content :deep(pre code) {
    padding: 40px 14px 14px;
    font-size: 12px;
  }

  .markdown-content :deep(table) {
    display: block;
    overflow-x: auto;
    white-space: nowrap;
  }
}
</style>
