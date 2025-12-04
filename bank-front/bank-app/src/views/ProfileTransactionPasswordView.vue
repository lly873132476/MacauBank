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
  password: '',
  confirmPassword: ''
})

const loading = ref(false)

// 设置交易密码
const setTransactionPassword = async () => {
  if (form.value.password !== form.value.confirmPassword) {
    showToast(t('auth.passwordMismatch'))
    return
  }
  
  if (form.value.password.length < 6) {
    showToast(t('auth.passwordTooShort'))
    return
  }
  
  loading.value = true
  try {
    const result = await authStore.updateTransactionPassword(form.value.password)
    if (result.success) {
      showToast(t('auth.transactionPasswordSetSuccess'))
      router.back()
    } else {
      showToast(result.message || t('auth.transactionPasswordSetFailed'))
    }
  } catch (error) {
    showToast(t('auth.transactionPasswordSetFailed'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="profile-transaction-password">
    <div class="profile-transaction-password-header">
      <van-nav-bar
        :title="t('profile.setTransactionPassword')"
        left-arrow
        @click-left="router.back()"
      />
    </div>
    
    <div class="profile-transaction-password-content">
      <van-form @submit="setTransactionPassword">
        <van-cell-group inset>
          <van-field
            v-model="form.password"
            :label="t('auth.transactionPassword')"
            :placeholder="t('auth.transactionPasswordPlaceholder')"
            type="password"
            required
          />
          
          <van-field
            v-model="form.confirmPassword"
            :label="t('auth.confirmTransactionPassword')"
            :placeholder="t('auth.confirmTransactionPasswordPlaceholder')"
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
.profile-transaction-password {
  min-height: 100vh;
  background: transparent;
  animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.profile-transaction-password-header :deep(.van-nav-bar) {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  box-shadow: 0 4px 12px rgba(79, 172, 254, 0.3);
}

.profile-transaction-password-header :deep(.van-nav-bar__title) {
  color: white;
  font-weight: 700;
  font-size: 18px;
}

.profile-transaction-password-header :deep(.van-icon) {
  color: white;
}

.profile-transaction-password-content {
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
  width: 100px;
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
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  padding: 14px;
  box-shadow: 0 4px 12px rgba(79, 172, 254, 0.3);
  transition: all 0.3s ease;
}

:deep(.van-button:active) {
  transform: translateY(1px);
}

/* 响应式优化 */
@media (max-width: 375px) {
  .profile-transaction-password-content {
    padding: 16px 10px;
  }
  
  :deep(.van-cell) {
    margin-bottom: 8px;
    min-height: 44px;
  }
  
  :deep(.van-field__label) {
    width: 90px;
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
  .profile-transaction-password-content {
    max-width: 600px;
    margin: 0 auto;
  }
}
</style>