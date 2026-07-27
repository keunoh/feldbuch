<script setup>
import {computed} from 'vue'

const props = defineProps({
  conversation: {
    type: Object,
    default: null
  }
});

const formattedCreatedAt = computed(() => {
  if (!props.conversation?.createdAt) {
    return '-';
  }

  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).format(new Date(props.conversation.createdAt))
});

const statusLabel = computed(() => {
  const status = props.conversation?.status

  const labels = {
    ACTIVE: '학습 중',
    COMPLETED: '학습 완료',
    ARCHIVED: '보관됨'
  }

  return labels[status] ?? status ?? '-'
});

</script>

<template>
  <aside class="study-info-panel">
    <h2 class="panel-title">
      📚 학습 정보
    </h2>

    <div
      v-if="conversation"
      class="info-list"
    >
      <section class="info-item">
        <span class="info-label">
          학습 주제
        </span>

        <strong class="info-value title-value">
          {{ conversation.title }}
        </strong>
      </section>

      <section class="info-item">
        <span class="info-label">
          상태
        </span>

        <span class="status-badge">
          {{ statusLabel }}
        </span>
      </section>

      <section class="info-item">
        <span class="info-label">
          상태
        </span>

        <span class="info-value">
          {{ conversation.messageCount ?? 0 }}개
        </span>
      </section>

      <section class="info-item">
        <span class="info-label">
          상태
        </span>

        <span class="info-value">
          {{ formattedCreatedAt }}
        </span>
      </section>
    </div>

    <p
      v-else
      class="empty-message"
    >
      대화를 선택하면<br>
      학습 정보가 표시됩니다.
    </p>

  </aside>
</template>

<style scoped>
.study-info-panel {
  width: 260px;
  min-width: 260px;
  padding: 24px;
  border-left: 1px solid #e5e7eb;
  background: #fafafa;
  box-sizing: border-box;
}

.panel-title {
  margin: 0 0 24px;
  font-size: 18px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-label {
  font-size: 13px;
  color: #6b7280;
}

.info-value {
  font-size: 15px;
  color: #111827;
}

.title-value {
  line-height: 1.5;
  overflow-wrap: anywhere;
}

.status-badge {
  width: fit-content;
  padding: 5px 10px;
  border-radius: 999px;
  background: #dcfce7;
  color: #166534;
  font-size: 13px;
  font-weight: 600;
}

.empty-message {
  margin-top: 40px;
  color: #9ca3af;
  font-size: 14px;
  line-height: 1.6;
  text-align: center;
}
</style>
