<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '../stores/user'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { isAuthError } from '../constants/errorCodes'

const { t } = useI18n()
const userStore = useUserStore()
const authStore = useAuthStore()
const router = useRouter()

const profile = ref<any>({
  username: '',
  name: '',
  mobile: '',
  idCard: ''
})

const loading = ref(false)

// 页面加载时获取用户信息
onMounted(async () => {
  const result = await userStore.fetchProfile()
  if (result.success) {
    // 直接使用后端返回的字段
    profile.value = {
      username: result.data.username || '',
      name: result.data.name || '',
      mobile: result.data.mobile || '',
      idCard: result.data.idCard || ''
    }
  } else {
    // 显示错误提示
    showToast({
      message: result.message || '获取用户信息失败',
      position: 'top'
    })
    
    // 通过错误码判断是否为认证错误 (xx2xx)
    if (result.code && isAuthError(result.code)) {
      // 清除登录状态
      await authStore.logout()
      // 延迟1秒后跳转
      setTimeout(() => {
        router.replace('/auth')
      }, 1000)
    }
  }
})

// 保存个人信息
const saveProfile = async () => {
  loading.value = true
  try {
    const result = await userStore.updateProfile(profile.value)
    if (result.success) {
      showToast(t('profile.saveSuccess'))
      router.back()
    } else {
      showToast(result.message || t('profile.saveFailed'))
    }
  } catch (error) {
    showToast(t('profile.saveFailed'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="profile-info">
    <div class="profile-info-header">
      <van-nav-bar
        :title="t('profile.personalInfo')"
        left-arrow
        @click-left="router.back()"
      />
    </div>
    
    <div class="profile-info-content">
      <van-form @submit="saveProfile">
        <van-cell-group inset>
          <van-field
            v-model="profile.username"
            label="用户名"
            placeholder="请输入用户名"
            maxlength="20"
            disabled
          />
          
          <van-field
            v-model="profile.name"
            label="真实姓名"
            placeholder="请输入真实姓名"
            maxlength="20"
            disabled
          />
          
          <van-field
            v-model="profile.mobile"
            label="手机号"
            placeholder="请输入手机号"
            type="tel"
            maxlength="11"
            disabled
          />
          
          <van-field
            v-model="profile.idCard"
            label="身份证号"
            placeholder="请输入身份证号"
            maxlength="18"
            disabled
          />
        </van-cell-group>
        
        <div style="margin: 24px 16px; color: #999; font-size: 12px; text-align: center;">
          个人信息暂不支持修改，如需修改请联系客服
        </div>
      </van-form>
    </div>
  </div>
</template>

<style scoped>
.profile-info {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf9 100%);
}

.profile-info-header :deep(.van-nav-bar) {
  background: linear-gradient(135deg, #007bff, #0056b3);
}

.profile-info-header :deep(.van-nav-bar__title) {
  color: white;
  font-weight: 600;
}

.profile-info-header :deep(.van-icon) {
  color: white;
}

.profile-info-content {
  padding: 20px 0;
}

:deep(.van-cell-group) {
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  margin: 0 20px;
}

:deep(.van-field__label) {
  font-weight: 500;
  color: #333;
}

:deep(.van-cell) {
  background-color: #ffffff;
}

:deep(.van-button) {
  font-size: 17px;
  font-weight: 600;
  padding: 14px 24px;
  border-radius: 30px;
}

/* 响应式优化 */
@media (max-width: 767px) {
  .profile-info-content {
    padding: 16px 0;
  }
  
  :deep(.van-cell-group) {
    margin: 0 16px;
    border-radius: 20px;
  }
}

@media (max-width: 480px) {
  .profile-info-content {
    padding: 12px 0;
  }
  
  :deep(.van-cell-group) {
    margin: 0 12px;
    border-radius: 18px;
  }
  
  :deep(.van-button) {
    font-size: 16px;
    padding: 12px 20px;
  }
}
</style>