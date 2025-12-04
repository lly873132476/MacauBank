<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import { authApi } from '../services/api'
import { showToast, showLoadingToast, closeToast } from 'vant'

const { t } = useI18n()
const authStore = useAuthStore()
const router = useRouter()

const activeTab = ref('login')
const loading = ref(false)
const loginForm = ref({
  username: '',
  password: ''
})

const registerForm = ref({
  step: 'basic',
  username: '',
  mobile: '',
  password: '',
  confirmPassword: '',
  name: '',
  idCard: '',
  initialCurrency: 'MOP'
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const transactionPasswordForm = ref({
  password: '',
  confirmPassword: ''
})

// 登录
const login = async () => {
  try {
    if (!loginForm.value.username) {
      showToast({
        message: '请输入用户名',
        position: 'top',
        icon: 'warning-o',
        className: 'custom-toast'
      })
      return
    }
    if (!loginForm.value.password) {
      showToast({
        message: '请输入密码',
        position: 'top',
        icon: 'warning-o',
        className: 'custom-toast'
      })
      return
    }
    
    loading.value = true
    const result = await authStore.login(loginForm.value.username, loginForm.value.password)
    if (result.success) {
      showToast({
        message: '登录成功',
        position: 'top',
        icon: 'checked',
        className: 'custom-toast-success'
      })
      // 跳转到首页
      router.push('/home')
    } else {
      showToast({
        message: result.message || '登录失败',
        position: 'top',
        icon: 'close',
        className: 'custom-toast-error'
      })
    }
  } catch (error) {
    console.error('登录错误:', error)
    showToast({
      message: '登录失败',
      position: 'top',
      icon: 'close',
      className: 'custom-toast-error'
    })
  } finally {
    loading.value = false
  }
}

// 注册下一步
const nextRegisterStep = async () => {
  console.log('=== nextRegisterStep 函数被调用 ===')
  console.log('当前步骤:', registerForm.value.step)
  console.log('表单数据:', registerForm.value)
  
  try {
    if (registerForm.value.step === 'basic') {
      console.log('执行第一步验证')
      // 验证必填字段
      if (!registerForm.value.username) {
        console.log('用户名为空')
        showToast({ message: '请输入用户名', position: 'top', icon: 'warning-o', className: 'custom-toast' })
        return
      }
      if (!registerForm.value.mobile) {
        console.log('手机号为空')
        showToast({ message: '请输入手机号', position: 'top', icon: 'warning-o', className: 'custom-toast' })
        return
      }
      if (!registerForm.value.password) {
        console.log('密码为空')
        showToast({ message: '请输入密码', position: 'top', icon: 'warning-o', className: 'custom-toast' })
        return
      }
      if (!registerForm.value.confirmPassword) {
        console.log('确认密码为空')
        showToast({ message: '请再次输入密码', position: 'top', icon: 'warning-o', className: 'custom-toast' })
        return
      }
      // 验证密码
      if (registerForm.value.password !== registerForm.value.confirmPassword) {
        console.log('两次密码不一致')
        console.log('password:', registerForm.value.password)
        console.log('confirmPassword:', registerForm.value.confirmPassword)
        try {
          showToast({ 
            message: '两次输入的密码不一致', 
            position: 'top', 
            icon: 'warning-o', 
            className: 'custom-toast',
            duration: 2000
          })
          console.log('Toast已调用')
        } catch (e) {
          console.error('Toast调用失败:', e)
        }
        return
      }
      if (registerForm.value.password.length < 6) {
        console.log('密码长度不足')
        showToast({ message: '密码长度不能少于6位', position: 'top', icon: 'warning-o', className: 'custom-toast' })
        return
      }
      
      console.log('验证通过，切换到详细信息步骤')
      registerForm.value.step = 'detail'
      console.log('步骤已更新为:', registerForm.value.step)
    } else {
      console.log('执行第二步验证和注册')
      // 验证第二步必填字段
      if (!registerForm.value.name) {
        console.log('姓名为空')
        showToast({ message: '请输入姓名', position: 'top', icon: 'warning-o', className: 'custom-toast' })
        return
      }
      if (!registerForm.value.idCard) {
        console.log('身份证为空')
        showToast({ message: '请输入身份证号', position: 'top', icon: 'warning-o', className: 'custom-toast' })
        return
      }
      
      // 执行注册
      loading.value = true
      console.log('开始调用注册API...')
      console.log('注册请求参数:', {
        username: registerForm.value.username,
        password: '***',
        name: registerForm.value.name,
        mobile: registerForm.value.mobile,
        idCard: registerForm.value.idCard,
        initialCurrency: registerForm.value.initialCurrency
      })
      
      const response = await authApi.registerExecute({
        username: registerForm.value.username,
        password: registerForm.value.password,
        name: registerForm.value.name,
        mobile: registerForm.value.mobile,
        idCard: registerForm.value.idCard,
        initialCurrency: registerForm.value.initialCurrency
      })
      
      console.log('注册API响应:', response)
      
      if (response.code === 200) {
        showToast({ message: '注册成功', position: 'top', icon: 'checked', className: 'custom-toast-success' })
        // 注册成功后自动登录
        loginForm.value.username = registerForm.value.username
        loginForm.value.password = registerForm.value.password
        console.log('准备自动登录')
        await login()
      } else {
        console.log('注册失败:', response.message)
        showToast({ message: response.message || '注册失败', position: 'top', icon: 'close', className: 'custom-toast-error' })
      }
    }
  } catch (error) {
    console.error('注册错误:', error)
    showToast({ message: '注册失败', position: 'top', icon: 'close', className: 'custom-toast-error' })
  } finally {
    loading.value = false
    console.log('=== nextRegisterStep 函数执行完毕 ===')
  }
}

// 注册上一步
const previousRegisterStep = () => {
  console.log('返回上一步')
  registerForm.value.step = 'basic'
}

// 修改登录密码
const updatePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    // 显示错误提示
    console.error('两次输入的密码不一致')
    return
  }
  
  const result = await authStore.updatePassword(
    passwordForm.value.oldPassword,
    passwordForm.value.newPassword
  )
  
  if (result.success) {
    // 显示成功提示
    console.log('密码修改成功')
  } else {
    // 显示错误提示
    console.error(result.message)
  }
}

// 设置/修改交易密码
const updateTransactionPassword = async () => {
  if (transactionPasswordForm.value.password !== transactionPasswordForm.value.confirmPassword) {
    // 显示错误提示
    console.error('两次输入的密码不一致')
    return
  }
  
  const result = await authStore.updateTransactionPassword(transactionPasswordForm.value.password)
  
  if (result.success) {
    // 显示成功提示
    console.log('交易密码设置成功')
  } else {
    // 显示错误提示
    console.error(result.message)
  }
}
</script>

<template>
  <div class="auth">
    <!-- 动态背景层 -->
    <div class="aurora-bg">
      <div class="aurora-blob blob-1"></div>
      <div class="aurora-blob blob-2"></div>
      <div class="aurora-blob blob-3"></div>
    </div>

    <div class="auth-container glass-card">
      <!-- Logo 区域 -->
      <div class="auth-logo">
        <div class="logo-icon-glow">
          <van-icon name="gem" size="48" />
        </div>
        <h1 class="logo-title">{{ t('app.title') }}</h1>
        <p class="logo-subtitle">未来金融 · 触手可及</p>
      </div>

      <!-- Tab 切换 -->
      <van-tabs v-model:active="activeTab" class="auth-tabs" animated swipeable>
        <!-- 登录 -->
        <van-tab name="login">
          <template #title>
            <span class="tab-label">{{ t('auth.login') }}</span>
          </template>
          
          <div class="auth-form">
            <div class="form-wrapper">
              <div class="form-item">
                <div class="input-wrapper-glass">
                  <van-icon name="manager-o" class="input-icon" />
                  <input
                    v-model="loginForm.username"
                    type="text"
                    class="custom-input"
                    placeholder="请输入用户名"
                  />
                </div>
              </div>
              
              <div class="form-item">
                <div class="input-wrapper-glass">
                  <van-icon name="shield-o" class="input-icon" />
                  <input
                    v-model="loginForm.password"
                    type="password"
                    class="custom-input"
                    placeholder="请输入密码"
                  />
                </div>
              </div>
              
              <div class="button-group">
                <button @click="login" class="btn-primary full-width" :disabled="loading" type="button">
                  <van-loading v-if="loading" size="20" color="white" />
                  <span v-else>{{ t('auth.login') }}</span>
                  <van-icon v-if="!loading" name="arrow" />
                </button>
              </div>
            </div>
          </div>
        </van-tab>

        <!-- 注册 -->
        <van-tab name="register">
          <template #title>
            <span class="tab-label">{{ t('auth.register') }}</span>
          </template>
          
          <div class="auth-form">
            <div class="form-wrapper">
              <transition name="slide-fade" mode="out-in">
                <div v-if="registerForm.step === 'basic'" key="basic">
                  <div class="form-item">
                    <div class="input-wrapper-glass">
                      <van-icon name="manager-o" class="input-icon" />
                      <input
                        v-model="registerForm.username"
                        type="text"
                        class="custom-input"
                        placeholder="设置用户名"
                      />
                    </div>
                  </div>
                  
                  <div class="form-item">
                    <div class="input-wrapper-glass">
                      <van-icon name="phone-o" class="input-icon" />
                      <input
                        v-model="registerForm.mobile"
                        type="tel"
                        class="custom-input"
                        placeholder="手机号码"
                      />
                    </div>
                  </div>
                  
                  <div class="form-item">
                    <div class="input-wrapper-glass">
                      <van-icon name="lock" class="input-icon" />
                      <input
                        v-model="registerForm.password"
                        type="password"
                        class="custom-input"
                        placeholder="设置密码"
                      />
                    </div>
                  </div>
                  
                  <div class="form-item">
                    <div class="input-wrapper-glass">
                      <van-icon name="shield-o" class="input-icon" />
                      <input
                        v-model="registerForm.confirmPassword"
                        type="password"
                        class="custom-input"
                        placeholder="确认密码"
                      />
                    </div>
                  </div>
                </div>

                <div v-else key="detail">
                  <div class="form-item">
                    <div class="input-wrapper-glass">
                      <van-icon name="idcard" class="input-icon" />
                      <input
                        v-model="registerForm.name"
                        type="text"
                        class="custom-input"
                        placeholder="真实姓名"
                      />
                    </div>
                  </div>
                  
                  <div class="form-item">
                    <div class="input-wrapper-glass">
                      <van-icon name="card" class="input-icon" />
                      <input
                        v-model="registerForm.idCard"
                        type="text"
                        class="custom-input"
                        placeholder="身份证号"
                      />
                    </div>
                  </div>
                </div>
              </transition>
              
              <!-- 按钮区域 -->
              <div class="button-group">
                <button 
                  v-if="registerForm.step === 'detail'"
                  @click="previousRegisterStep" 
                  class="btn-secondary-glass" 
                  type="button"
                >
                  <van-icon name="arrow-left" />
                </button>
                
                <button 
                  @click="nextRegisterStep" 
                  class="btn-primary full-width"
                  :disabled="loading" 
                  type="button"
                >
                  <van-loading v-if="loading" size="20" color="white" />
                  <span v-else>{{ registerForm.step === 'basic' ? '下一步' : '完成注册' }}</span>
                  <van-icon v-if="!loading && registerForm.step === 'basic'" name="arrow" />
                  <van-icon v-if="!loading && registerForm.step === 'detail'" name="success" />
                </button>
              </div>
            </div>
          </div>
        </van-tab>
      </van-tabs>
    </div>
  </div>
</template>

<style scoped>
.auth {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
  background: #0f0c29; /* 深色底色 */
}

/* 极光背景动画 */
.aurora-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
}

.aurora-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
  animation: float 10s infinite ease-in-out;
}

.blob-1 {
  top: -10%;
  left: -10%;
  width: 500px;
  height: 500px;
  background: #6C5DD3;
}

.blob-2 {
  bottom: -10%;
  right: -10%;
  width: 400px;
  height: 400px;
  background: #00D2FF;
  animation-delay: -2s;
}

.blob-3 {
  top: 40%;
  left: 30%;
  width: 300px;
  height: 300px;
  background: #FF6B6B;
  animation-delay: -5s;
  opacity: 0.4;
}

/* 容器 */
.auth-container {
  width: 100%;
  max-width: 400px;
  position: relative;
  z-index: 10;
  padding: 40px 20px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 25px 50px rgba(0,0,0,0.2);
}

.dark .auth-container {
  background: rgba(0, 0, 0, 0.4);
  border-color: rgba(255, 255, 255, 0.1);
}

/* Logo 区域 */
.auth-logo {
  text-align: center;
  margin-bottom: 32px;
}

.logo-icon-glow {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #6C5DD3, #00D2FF);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 0 30px rgba(108, 93, 211, 0.6);
  animation: breathe 4s infinite ease-in-out;
}

.logo-title {
  font-size: 28px;
  font-weight: 800;
  color: white;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.logo-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
  font-weight: 500;
  letter-spacing: 2px;
  text-transform: uppercase;
}

/* Tab 样式 */
.auth-tabs {
  background: transparent;
}

.auth-tabs :deep(.van-tabs__nav) {
  background: transparent;
}

.auth-tabs :deep(.van-tab) {
  color: rgba(255, 255, 255, 0.6);
  font-size: 16px;
  font-weight: 600;
}

.auth-tabs :deep(.van-tab--active) {
  color: white;
}

.auth-tabs :deep(.van-tabs__line) {
  background: linear-gradient(90deg, #00D2FF, #6C5DD3);
  height: 4px;
  border-radius: 4px;
  bottom: 0;
}

/* 输入框样式 */
.form-item {
  margin-bottom: 20px;
}

.input-wrapper-glass {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
  height: 56px;
  padding: 0 16px;
}

.input-wrapper-glass:focus-within {
  border-color: rgba(255, 255, 255, 0.5);
  background: rgba(0, 0, 0, 0.4);
  box-shadow: 0 0 20px rgba(108, 93, 211, 0.2);
}

.input-icon {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.5);
  margin-right: 12px;
}

.input-wrapper-glass:focus-within .input-icon {
  color: white;
}

.custom-input {
  flex: 1;
  height: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 16px;
  color: white;
}

.custom-input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

/* 按钮 */
.full-width {
  width: 100%;
  flex: 1;
}

.btn-secondary-glass {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.1);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-secondary-glass:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* 动画 */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* Toast 样式修复 */
:deep(.custom-toast) {
  background: rgba(30, 30, 40, 0.9) !important;
  backdrop-filter: blur(10px) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
}
</style>