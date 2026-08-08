<script setup>
import BaseTerminalHeader from "@/components/common/BaseTerminalHeader.vue";
import BaseButton from "@/components/common/BaseButton.vue";
import BaseBadge from "@/components/common/BaseBadge.vue";
import BaseModal from "@/components/common/BaseModal.vue";
import SettingsSection from "@/components/common/settings/SettingsSection.vue";
import SettingRow from "@/components/common/settings/SettingRow.vue";
import SettingsList from "@/components/common/settings/SettingsList.vue";
import BaseStatusLabel from "@/components/common/BaseStatusLabel.vue";
import BaseTerminalCommand from "@/components/common/BaseTerminalCommand.vue";

const props = defineProps({
  user: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits([
  'close',
  'logout',
])

function close() {
  emit(
    'close',
  )
}

function logout() {
  emit(
    'logout',
  )
}
</script>

<template>
  <BaseModal
    aria-label="Feldbuch 설정"
    @close="close"
  >
    <BaseTerminalHeader
      class="settings-terminal-header"
      title="feldbuch://settings"
    >
      <template #actions>
        <button
          type="button"
          class="close-button"
          aria-label="설정 닫기"
          @click="close"
        >
          ×
        </button>
      </template>

      <BaseTerminalCommand>
        feldbuch config --list
      </BaseTerminalCommand>
    </BaseTerminalHeader>

    <div
      v-if="props.user"
      class="settings-content"
    >
      <SettingsSection
        index="01"
        title="Account"
        description="Feldbuch 사용자 계정 정보"
      >
        <SettingsList>
          <SettingRow label="nickname">
            {{ props.user.nickname }}
          </SettingRow>

          <SettingRow label="email">
            {{ props.user.email }}
          </SettingRow>

          <SettingRow
            label="user_id"
            mono
          >
            {{ props.user.userId }}
          </SettingRow>

          <SettingRow label="role">
            <BaseBadge>
              {{ props.user.role }}
            </BaseBadge>
          </SettingRow>
        </SettingsList>
      </SettingsSection>

      <SettingsSection
        index="02"
        title="Authentication"
        description="현재 세션의 인증 정보"
      >
        <SettingsList>
          <SettingRow label="provider">
            <BaseBadge variant="success">
              {{ props.user.provider }}
            </BaseBadge>
          </SettingRow>

          <SettingRow label="status">
            <BaseStatusLabel pulse>
              authenticated
            </BaseStatusLabel>
          </SettingRow>

          <SettingRow
            label="token_type"
            mono
          >
            Bearer
          </SettingRow>
        </SettingsList>
      </SettingsSection>

      <SettingsSection
        index="03"
        title="Appearance"
        description="Feldbuch 인터페이스 설정"
      >
        <SettingsList>
          <SettingRow label="theme">
            <BaseBadge>
              TERMINAL DARK
            </BaseBadge>
          </SettingRow>

          <SettingRow label="accent">
            <BaseStatusLabel>
              feldbuch green
            </BaseStatusLabel>
          </SettingRow>

          <SettingRow
            label="font"
            mono
          >
            JetBrains Mono
          </SettingRow>
        </SettingsList>

        <p class="coming-soon">
          # appearance customization coming soon
        </p>
      </SettingsSection>


      <footer class="settings-footer">
        <BaseButton
          type="button"
          variant="secondary"
          size="sm"
          @click="close"
        >
          <template #prefix>
            ❮
          </template>

          close
        </BaseButton>

        <BaseButton
          type="button"
          variant="danger"
          size="sm"
          @click="logout"
        >
          <template #prefix>
            ❯
          </template>

          logout
        </BaseButton>
      </footer>
    </div>
  </BaseModal>
</template>

<style scoped>
.close-button {
  justify-self: end;
  width: 28px;
  height: 28px;
  padding: 0;
  border: 0;
  border-radius: var(--radius-5);
  color: rgba(210, 229, 216, 0.42);
  background: transparent;
  font-size: 17px;
  cursor: pointer;
}

.close-button:hover {
  color: var(--color-primary);
  background: var(--color-terminal-primary-soft-strong);
}

.settings-content {
  padding: 0 27px 27px;
}

.coming-soon {
  margin: var(--space-5) 0 0;
  color: rgba(177, 201, 185, 0.27);
  font-family: var(--font-family-terminal);
  font-size: 8px;
}

.settings-footer {
  display: flex;
  justify-content: space-between;
  gap: var(--space-5);
  padding-top: var(--space-10);
}

@media (
max-width: 650px
) {
  .settings-content {
    padding: 0 var(--space-8) var(--space-9);
  }

  .settings-footer {
    flex-direction: column;
  }
}

@media (max-width: 650px) {
  .settings-footer :deep(.base-button) {
    width: 100%;
  }
}
</style>
