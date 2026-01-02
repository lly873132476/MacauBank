<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import { authApi } from '../services/api'
import { showToast, showDialog } from 'vant'

const { t } = useI18n()
const authStore = useAuthStore()
const router = useRouter()

const activeTab = ref('login')
const loading = ref(false)

// 登录类型: 'password' | 'sms'
const loginType = ref('password')

// 登录表单
const loginForm = reactive({
  userName: '',
  password: '',
  mobile: '',
  verifyCode: ''
})

// 注册表单
const registerForm = reactive({
  mobilePrefix: '+853',
  mobile: '',
  verifyCode: '',
  password: '',
  confirmPassword: ''
})

// 验证码倒计时
const countdown = ref(0)
const isCounting = ref(false)

// 发送验证码逻辑
const sendCode = async (mobile: string) => {
  if (!mobile) return showToast('请输入手机号')
  // 这里可以加正则校验
  
  if (isCounting.value) return
  
  try {
    // 模拟调用后端发送接口 (实际需对接 VerifyCodeController)
    // await authApi.sendVerifyCode(mobile) 
    
    showToast({ message: '验证码已发送 (模拟: 123456)', icon: 'comment-o' })
    
    // 开始倒计时
    isCounting.value = true
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
        isCounting.value = false
      }
    }, 1000)
  } catch (e) {
    showToast('发送失败')
  }
}

// 登录逻辑
const login = async () => {
  loading.value = true
  try {
    let result
    if (loginType.value === 'password') {
      // 密码登录
      if (!loginForm.userName || !loginForm.password) {
        showToast('请输入账号和密码')
        return
      }
      result = await authStore.login(loginForm.userName, loginForm.password)
    } else {
      // 验证码登录
      if (!loginForm.mobile || !loginForm.verifyCode) {
        showToast('请输入手机号和验证码')
        return
      }
      // 调用 Store 的验证码登录方法 (需在 Store 中扩展，或直接调 API)
      // 这里为了演示，直接调用 API 并手动处理 Store 状态
      const res = await authApi.login({
        userName: loginForm.mobile, // 手机号作为 userName 传参
        loginType: 'SMS',
        verifyCode: loginForm.verifyCode
      })
      if (res.code === 200) {
        authStore.setToken(res.data.token)
        authStore.setUserInfo(res.data)
        result = { success: true }
      } else {
        result = { success: false, message: res.message }
      }
    }

    if (result.success) {
      showToast({ message: '欢迎回来', icon: 'checked', className: 'custom-toast-success' })
      router.push('/home')
    } else {
      showToast({ message: result.message || '登录失败', icon: 'close', className: 'custom-toast-error' })
    }
  } catch (error) {
    showToast({ message: '系统繁忙', icon: 'close', className: 'custom-toast-error' })
  } finally {
    loading.value = false
  }
}

// 注册逻辑 (手机号+验证码)
const handleRegister = async () => {
  const form = registerForm

  if (!form.mobile) return showToast('请输入手机号')
  if (!form.verifyCode) return showToast('请输入验证码')
  if (!form.password || form.password.length < 8) return showToast('密码至少8位')
  if (form.password !== form.confirmPassword) return showToast('两次密码不一致')

  loading.value = true
  try {
    const apiPayload = {
      // userName 留空，由后端根据手机号生成
      mobilePrefix: form.mobilePrefix,
      mobile: form.mobile,
      verifyCode: form.verifyCode,
      password: form.password
    }

    const response = await authApi.registerExecute(apiPayload)

    if (response.code === 200) {
      // 优先处理自动登录
      if (response.data && response.data.token) {
        showToast({ message: '注册成功', icon: 'checked', className: 'custom-toast-success' })
        
        authStore.setToken(response.data.token)
        authStore.setUserInfo({
          userId: response.data.userNo || response.data.userId,
          userName: response.data.userName,
          name: response.data.name || ''
        })
        
        // 立即跳转，避免停留在当前页产生误解
        router.replace('/home')
      } else {
        // 如果后端确实没返回Token，则引导去登录
        showToast({ message: '注册成功，请登录', icon: 'checked' })
        activeTab.value = 'login'
        loginType.value = 'password'
        loginForm.userName = response.data.userName
      }
    } else {
      showToast({ message: response.message || '注册失败', icon: 'close', className: 'custom-toast-error' })
    }
  } catch (error) {
    console.error(error)
    showToast({ message: '服务暂时不可用', icon: 'close', className: 'custom-toast-error' })
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth">
    <!-- 动态极光背景 -->
    <div class="aurora-bg">
      <div class="aurora-blob blob-1"></div>
      <div class="aurora-blob blob-2"></div>
      <div class="aurora-blob blob-3"></div>
    </div>

    <div class="auth-container glass-card">
      <!-- 品牌 Logo -->
      <div class="auth-logo">
        <div class="logo-icon-glow">
          <van-icon name="gem" size="48" />
        </div>
        <h1 class="logo-title">{{ t('app.title') }}</h1>
        <p class="logo-subtitle">M A C A U &nbsp; B A N K</p>
      </div>

      <!-- 主标签页 (登录/注册) -->
      <van-tabs v-model:active="activeTab" class="auth-tabs" animated swipeable background="transparent">
        
        <!-- ============ 登录页 ============ -->
        <van-tab name="login" title="账户登录">
          <div class="auth-form-body">
            
            <!-- 登录方式切换 -->
            <div class="login-type-switch mb-4">
              <span 
                :class="{ active: loginType === 'password' }" 
                @click="loginType = 'password'"
              >密码登录</span>
              <span class="divider">|</span>
              <span 
                :class="{ active: loginType === 'sms' }" 
                @click="loginType = 'sms'"
              >验证码登录</span>
            </div>

            <!-- 密码登录表单 -->
            <div v-if="loginType === 'password'" class="fade-in">
              <div class="form-item">
                <div class="input-wrapper-glass">
                  <van-icon name="manager-o" class="input-icon" />
                  <input v-model="loginForm.userName" type="text" class="custom-input" placeholder="用户名 / 手机号" />
                </div>
              </div>
              <div class="form-item">
                <div class="input-wrapper-glass">
                  <van-icon name="shield-o" class="input-icon" />
                  <input v-model="loginForm.password" type="password" class="custom-input" placeholder="登录密码" @keyup.enter="login" />
                </div>
              </div>
            </div>

            <!-- 验证码登录表单 -->
            <div v-else class="fade-in">
              <div class="form-item">
                <div class="input-wrapper-glass">
                  <van-icon name="phone-o" class="input-icon" />
                  <input v-model="loginForm.mobile" type="tel" class="custom-input" placeholder="手机号码" />
                </div>
              </div>
              <div class="form-item">
                <div class="input-wrapper-glass">
                  <van-icon name="comment-o" class="input-icon" />
                  <input v-model="loginForm.verifyCode" type="tel" class="custom-input" placeholder="验证码" />
                  <span class="verify-btn" :class="{ disabled: isCounting }" @click="sendCode(loginForm.mobile)">
                    {{ isCounting ? `${countdown}s` : '获取验证码' }}
                  </span>
                </div>
              </div>
            </div>
            
            <button @click="login" class="btn-primary full-width mt-4" :disabled="loading">
              <van-loading v-if="loading" size="20" />
              <span v-else>立即登录</span>
            </button>
            
            <p class="text-center mt-4 text-xs text-white/50">忘记密码？请联系客服热线 (853) 2888 8888</p>
          </div>
        </van-tab>

        <!-- ============ 注册页 (手机号+验证码) ============ -->
        <van-tab name="register" title="快速注册">
          <div class="auth-form-body">
            
            <div class="form-item">
              <div class="input-wrapper-glass">
                <div class="prefix-select">+853</div>
                <div class="divider-v"></div>
                <input v-model="registerForm.mobile" type="tel" class="custom-input" placeholder="手机号码" />
              </div>
            </div>

            <div class="form-item">
              <div class="input-wrapper-glass">
                <van-icon name="comment-o" class="input-icon" />
                <input v-model="registerForm.verifyCode" type="tel" class="custom-input" placeholder="短信验证码" />
                <span class="verify-btn" :class="{ disabled: isCounting }" @click="sendCode(registerForm.mobile)">
                  {{ isCounting ? `${countdown}s` : '获取验证码' }}
                </span>
              </div>
            </div>

            <div class="form-item">
              <div class="input-wrapper-glass">
                <van-icon name="lock" class="input-icon" />
                <input v-model="registerForm.password" type="password" class="custom-input" placeholder="设置登录密码 (8-20位)" />
              </div>
            </div>
            
            <div class="form-item">
              <div class="input-wrapper-glass">
                <van-icon name="lock" class="input-icon" />
                <input v-model="registerForm.confirmPassword" type="password" class="custom-input" placeholder="确认登录密码" />
              </div>
            </div>

            <button @click="handleRegister" class="btn-primary full-width mt-4" :disabled="loading">
              <van-loading v-if="loading" size="20" />
              <span v-else>立即注册</span>
            </button>
            
            <p class="text-center mt-4 text-xs text-white/50">
              注册即代表您同意 <span style="color: #00D2FF; cursor: pointer;">《服务条款》</span> 及 <span style="color: #00D2FF; cursor: pointer;">《隐私政策》</span>
            </p>

          </div>
        </van-tab>
      </van-tabs>
    </div>
  </div>
</template>

<style scoped>
/* 核心布局 */
.auth {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  position: relative;
  background: #0f0c29;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 极光背景 */
.aurora-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  background: radial-gradient(circle at 50% 120%, #1a1a2e 0%, #0f0c29 100%);
}
.aurora-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 10s infinite ease-in-out;
}
.blob-1 { top: -20%; left: -20%; width: 600px; height: 600px; background: #6C5DD3; }
.blob-2 { bottom: -20%; right: -20%; width: 500px; height: 500px; background: #00D2FF; animation-delay: -3s; }
.blob-3 { top: 40%; left: 30%; width: 400px; height: 400px; background: #FF6B6B; animation-delay: -6s; opacity: 0.3; }

/* 玻璃拟态容器 */
.auth-container {
  width: 100%;
  max-width: 380px; 
  position: relative;
  z-index: 10;
  padding: 32px 24px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 24px;
  box-shadow: 0 30px 60px rgba(0,0,0,0.3);
  backdrop-filter: blur(20px);
}

/* Logo */
.auth-logo { text-align: center; margin-bottom: 24px; }
.logo-icon-glow {
  width: 64px; height: 64px; margin: 0 auto 12px;
  background: linear-gradient(135deg, #6C5DD3, #00D2FF);
  border-radius: 20px;
  display: flex; align-items: center; justify-content: center;
  color: white;
  box-shadow: 0 0 25px rgba(108, 93, 211, 0.5);
}
.logo-title { font-size: 24px; font-weight: 700; color: white; margin: 0; letter-spacing: 1px; }
.logo-subtitle { font-size: 12px; color: rgba(255,255,255,0.6); letter-spacing: 3px; margin-top: 4px; }

/* 登录方式切换 */
.login-type-switch {
  display: flex;
  justify-content: center;
  align-items: center;
  color: rgba(255,255,255,0.4);
  font-size: 14px;
  gap: 12px;
}
.login-type-switch span.active {
  color: white;
  font-weight: 600;
  position: relative;
}
.login-type-switch span.active::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  width: 16px;
  height: 2px;
  background: #00D2FF;
  border-radius: 2px;
}
.divider { color: rgba(255,255,255,0.1) !important; font-weight: 100 !important; }

/* 表单元素 */
.auth-form-body { margin-top: 16px; }
.form-item { margin-bottom: 20px; }

.input-wrapper-glass {
  display: flex; align-items: center;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  height: 52px;
  padding: 0 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.input-wrapper-glass:focus-within {
  border-color: rgba(0, 210, 255, 0.6);
  background: rgba(0, 0, 0, 0.3);
  box-shadow: 0 0 15px rgba(0, 210, 255, 0.15);
}

.input-icon { color: rgba(255,255,255,0.5); font-size: 18px; margin-right: 12px; }
.custom-input {
  flex: 1; height: 100%; background: transparent; border: none; outline: none;
  color: white; font-size: 15px; width: 100%;
}
.custom-input::placeholder { color: rgba(255,255,255,0.3); }

/* 验证码按钮 */
.verify-btn {
  font-size: 13px;
  color: #00D2FF;
  cursor: pointer;
  padding: 4px 8px;
  margin-left: 8px;
  white-space: nowrap;
}
.verify-btn.disabled { color: rgba(255,255,255,0.3); cursor: not-allowed; }

.prefix-select { color: white; font-weight: 500; font-size: 15px; }
.divider-v { width: 1px; height: 20px; background: rgba(255,255,255,0.2); margin: 0 12px; }

/* 按钮 */
.full-width { width: 100%; }
.mt-4 { margin-top: 16px; }
.mb-4 { margin-bottom: 24px; }
.text-center { text-align: center; }
.text-xs { font-size: 12px; }

.btn-primary {
  background: linear-gradient(90deg, #6C5DD3, #00D2FF);
  border: none; border-radius: 12px;
  height: 50px; color: white; font-weight: 600; font-size: 16px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: opacity 0.2s;
  box-shadow: 0 4px 15px rgba(108, 93, 211, 0.4);
}
.btn-primary:active { opacity: 0.9; transform: scale(0.98); }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

/* 动画 */
.fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 覆盖 Vant 样式 */
:deep(.van-tabs__nav) { background: transparent !important; }
:deep(.van-tab) { color: rgba(255,255,255,0.5); font-weight: 600; }
:deep(.van-tab--active) { color: white; }
:deep(.van-tabs__line) { background: #00D2FF; bottom: 6px; width: 20px !important; height: 3px; }
</style>