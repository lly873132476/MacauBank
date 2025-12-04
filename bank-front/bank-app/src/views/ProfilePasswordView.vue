<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'

const { t } = useI18n()
const authStore = useAuthStore()
const router = useRouter()

const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const loading = ref(false)

// 修改密码
const updatePassword = async () => {
  if (form.value.newPassword !== form.value.confirmPassword) {
    showToast(t('auth.passwordMismatch'))
    return
  }
  
  if (form.value.newPassword.length < 6) {
    showToast(t('auth.passwordTooShort'))
    return
  }
  
  loading.value = true
  try {
    const result = await authStore.updatePassword(
      form.value.oldPassword,
      form.value.newPassword
    )
    if (result.success) {
      showToast(t('auth.passwordUpdateSuccess'))
      router.back()
    } else {
      showToast(result.message || t('auth.passwordUpdateFailed'))
    }
  } catch (error) {
    showToast(t('auth.passwordUpdateFailed'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="profile-password">
    <div class="profile-password-header">
      <van-nav-bar
        :title="t('profile.updatePassword')"
        left-arrow
        @click-left="router.back()"
      />
    </div>
    
    <div class="profile-password-content">
      <van-form @submit="updatePassword">
        <van-cell-group inset>
          <van-field
            v-model="form.oldPassword"
            :label="t('auth.oldPassword')"
            :placeholder="t('auth.oldPasswordPlaceholder')"
            type="password"
            required
          />
          
          <van-field
            v-model="form.newPassword"
            :label="t('auth.newPassword')"
            :placeholder="t('auth.newPasswordPlaceholder')"
            type="password"
            required
          />
          
          <van-field
            v-model="form.confirmPassword"
            :label="t('auth.confirmPassword')"
            :placeholder="t('auth.confirmPasswordPlaceholder')"
            type="password"
            required
          />
        </van-cell-group>
        
        <div style="margin: 24px 16px;">
          <van-button 
            round 
            block 
            type="primary" 
            native-type="submit"
            :loading="loading"
          >
            {{ t('profile.save') }}
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<style scoped>
.profile-password {
  min-height: 100vh;
  background: transparent;
  animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.profile-password-header :deep(.van-nav-bar) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.profile-password-header :deep(.van-nav-bar__title) {
  color: white;
  font-weight: 700;
  font-size: 18px;
}

.profile-password-header :deep(.van-icon) {
  color: white;
}

.profile-password-content {
  padding: 20px 12px;
}

:deep(.van-cell-group) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin: 0;
  background: transparent;
}

:deep(.van-cell) {
  background: #f9fafb;
  border-radius: 12px;
  margin-bottom: 10px;
  padding: 10px 16px;
  border: 1px solid #e5e7eb;
  min-height: 48px;
}

.dark :deep(.van-cell) {
  background: #1e1e2e;
  border-color: rgba(255, 255, 255, 0.1);
}

:deep(.van-field__label) {
  font-weight: 600;
  color: #4b5563;
  font-size: 14px;
  width: 80px;
}

.dark :deep(.van-field__label) {
  color: #d1d5db;
}

:deep(.van-field__control) {
  font-size: 15px;
  color: #1a1a2e;
}

.dark :deep(.van-field__control) {
  color: #ffffff;
}

:deep(.van-button) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  padding: 14px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

:deep(.van-button:active) {
  transform: translateY(1px);
}

/* 响应式优化 */
@media (max-width: 375px) {
  .profile-password-content {
    padding: 16px 10px;
  }
  
  :deep(.van-cell) {
    margin-bottom: 8px;
    min-height: 44px;
  }
  
  :deep(.van-button) {
    font-size: 15px;
    padding: 12px;
  }
}

@media (min-width: 481px) {
  :deep(.van-cell) {
    min-height: 50px;
  }
}

@media (min-width: 769px) {
  .profile-password-content {
    max-width: 600px;
    margin: 0 auto;
  }
}
</style>