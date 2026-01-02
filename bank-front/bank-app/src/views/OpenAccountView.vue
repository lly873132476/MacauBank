<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useUserStore } from '../stores/user'
import { showToast, showLoadingToast, closeToast, showDialog } from 'vant'

const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()

const activeStep = ref(0)
const loading = ref(false)

const steps = [
  { text: '身份信息', icon: 'idcard' },
  { text: '背景调查', icon: 'manager-o' },
  { text: '合规声明', icon: 'passed' }
]

const form = ref({
  // Step 1: Identity
  nameCn: '',
  nameEn: '',
  idCardType: 1, // 1-澳门身份证
  idCardNo: '',
  expiryDate: '',
  gender: 1, // 1-男, 2-女
  birthday: '',
  issueCountry: 'Macau',
  issueOrg: 'DSI',
  idCardFront: [] as any[],
  idCardBack: [] as any[],
  
  // Step 2: Profile
  employmentStatus: '1', // 1-受雇
  occupation: '',
  addressRegion: 'Macau Peninsula',
  addressDetail: '',
  taxId: '',
  
  // Step 3: Compliance
  sourceOfFunds: '',
  openPurpose: '',
  isUSPerson: false
})

// Options
const issueOrgs = [
  { text: '身份证明局 (DSI)', value: 'DSI' },
  { text: '治安警察局 (CPSP)', value: 'CPSP' },
  { text: '中国外交部', value: 'MFA' }
]

const employmentTypes = [
  { text: '受雇人士', value: '1' },
  { text: '自雇/独资经营', value: '2' },
  { text: '待业/退休', value: '3' },
  { text: '学生', value: '4' }
]

const regions = ['澳门半岛', '氹仔', '路环', '横琴深合区']

const onNext = () => {
  if (activeStep.value === 0) {
    if (form.value.idCardFront.length === 0) return showToast('请上传证件人像面')
    if (!form.value.nameCn || !form.value.idCardNo) return showToast('请完善身份信息')
    
    // 模拟OCR填充
    if (!form.value.nameEn) form.value.nameEn = 'CHAN TAI MAN'
    
    activeStep.value = 1
  } else if (activeStep.value === 1) {
    if (!form.value.occupation) return showToast('请输入职业')
    if (!form.value.addressDetail) return showToast('请输入详细地址')
    activeStep.value = 2
  }
}

const onPrev = () => {
  if (activeStep.value > 0) activeStep.value--
}

const onSubmit = async () => {
  if (!form.value.sourceOfFunds) return showToast('请选择资金来源')
  if (form.value.isUSPerson) return showDialog({ message: '抱歉，暂不支持美国纳税人在线开户，请前往柜台办理。' })
  
  loading.value = true
  
  try {
    const result = await userStore.submitOpenAccount(form.value)
    
    if (result.success) {
      authStore.completeKyc()
      
      showDialog({
        title: '申请已受理',
        message: '您的数字账户开立申请已提交，系统审核通过。现在您可以开始使用账户功能。',
        theme: 'round-button',
        confirmButtonColor: '#2563EB'
      }).then(() => {
        router.replace('/home')
      })
    } else {
      showToast(result.message || '提交失败，请重试')
    }
  } catch (error) {
    showToast('网络异常，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page-layout">
    <!-- 顶部导航 (Fixed) -->
    <div class="header-nav">
      <div class="back-btn" @click="activeStep > 0 ? onPrev() : router.back()">
        <van-icon name="arrow-left" />
      </div>
      <span class="nav-title">开立数字账户</span>
      <div class="placeholder"></div>
    </div>

    <!-- 滚动内容区 (Flex Grow) -->
    <div class="scroll-container">
      
      <!-- 进度条 -->
      <div class="progress-section">
        <div class="step-indicator">
          <div 
            v-for="(step, index) in steps" 
            :key="index"
            class="step-item"
            :class="{ active: index <= activeStep }"
          >
            <div class="circle">
              <van-icon v-if="index < activeStep" name="success" />
              <span v-else>{{ index + 1 }}</span>
            </div>
            <span class="label">{{ step.text }}</span>
          </div>
        </div>
        <div class="progress-line">
          <div class="fill" :style="{ width: `${activeStep * 50}%` }"></div>
        </div>
      </div>

      <!-- 表单内容 -->
      <div class="form-content">
        
        <!-- Step 1: Identity -->
        <div v-if="activeStep === 0" class="step-panel fade-in">
          <div class="section-card">
            <div class="section-head">
              <h3>证件影像</h3>
              <span class="sub">请上传有效的澳门居民身份证</span>
            </div>
            <div class="upload-row">
              <van-uploader v-model="form.idCardFront" :max-count="1" class="upload-box">
                <div class="upload-placeholder">
                  <div class="skeleton-card">
                    <van-icon name="photograph" class="bg-icon" />
                    <div class="plus-circle">
                      <van-icon name="plus" />
                    </div>
                  </div>
                  <span class="upload-text">上传人像面</span>
                </div>
                <template #preview-cover>
                  <div class="preview-mask">
                    <van-icon name="passed" />
                    <span>点击更换</span>
                  </div>
                </template>
              </van-uploader>
              <van-uploader v-model="form.idCardBack" :max-count="1" class="upload-box">
                <div class="upload-placeholder">
                  <div class="skeleton-card">
                    <van-icon name="card" class="bg-icon" />
                    <div class="plus-circle">
                      <van-icon name="plus" />
                    </div>
                  </div>
                  <span class="upload-text">上传国徽面</span>
                </div>
                <template #preview-cover>
                  <div class="preview-mask">
                    <van-icon name="passed" />
                    <span>点击更换</span>
                  </div>
                </template>
              </van-uploader>
            </div>
          </div>

          <div class="section-card mt-6">
            <div class="section-head">
              <h3>身份信息</h3>
              <span class="sub">请核对OCR识别结果</span>
            </div>
            <div class="form-grid">
              <div class="input-field full">
                <label>中文姓名</label>
                <input v-model="form.nameCn" type="text" placeholder="与证件一致" />
              </div>
              <div class="input-field full">
                <label>葡文/英文姓名</label>
                <input v-model="form.nameEn" type="text" placeholder="CHAN TAI MAN" />
              </div>
              <div class="input-field">
                <label>性别</label>
                <div class="radio-group simple">
                  <div 
                    class="radio-pill mini" 
                    :class="{ active: form.gender === 1 }"
                    @click="form.gender = 1"
                  >男</div>
                  <div 
                    class="radio-pill mini" 
                    :class="{ active: form.gender === 2 }"
                    @click="form.gender = 2"
                  >女</div>
                </div>
              </div>
              <div class="input-field">
                <label>出生日期</label>
                <input v-model="form.birthday" type="date" class="date-input" />
              </div>
              <div class="input-field">
                <label>证件号码</label>
                <input v-model="form.idCardNo" type="text" maxlength="8" />
              </div>
              <div class="input-field">
                <label>有效期至</label>
                <input v-model="form.expiryDate" type="date" class="date-input" />
              </div>
              <div class="input-field">
                <label>发证地</label>
                <div class="select-wrapper">
                  <select v-model="form.issueCountry">
                    <option value="Macau">中国澳门</option>
                    <option value="China">中国内地</option>
                    <option value="HongKong">中国香港</option>
                  </select>
                  <van-icon name="arrow-down" />
                </div>
              </div>
              <div class="input-field">
                <label>发证机关</label>
                <div class="select-wrapper">
                  <select v-model="form.issueOrg">
                    <option v-for="org in issueOrgs" :key="org.value" :value="org.value">{{ org.text }}</option>
                  </select>
                  <van-icon name="arrow-down" />
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Step 2: Profile -->
        <div v-if="activeStep === 1" class="step-panel fade-in">
          <div class="section-card">
            <div class="section-head">
              <h3>职业信息</h3>
            </div>
            <div class="form-grid">
              <div class="input-field full">
                <label>就业状态</label>
                <div class="radio-group">
                  <div 
                    v-for="type in employmentTypes" 
                    :key="type.value"
                    class="radio-pill"
                    :class="{ active: form.employmentStatus === type.value }"
                    @click="form.employmentStatus = type.value"
                  >
                    {{ type.text }}
                  </div>
                </div>
              </div>
              <div class="input-field full">
                <label>职业/职位</label>
                <input v-model="form.occupation" type="text" placeholder="如: 软件工程师" />
              </div>
            </div>
          </div>

          <div class="section-card mt-6">
            <div class="section-head">
              <h3>居住地址</h3>
            </div>
            <div class="form-grid">
              <div class="input-field full">
                <label>地区</label>
                <div class="region-tags">
                  <span 
                    v-for="reg in regions" :key="reg" 
                    class="tag" 
                    :class="{ active: form.addressRegion === reg }"
                    @click="form.addressRegion = reg"
                  >{{ reg }}</span>
                </div>
              </div>
              <div class="input-field full">
                <label>详细地址</label>
                <textarea v-model="form.addressDetail" rows="2" placeholder="街道、大厦、座数、楼层、单位"></textarea>
              </div>
            </div>
          </div>

          <div class="section-card mt-6">
            <div class="section-head">
              <h3>税务信息 (选填)</h3>
            </div>
            <div class="form-grid">
              <div class="input-field full">
                <label>税务编号 (TIN)</label>
                <input v-model="form.taxId" type="text" placeholder="如有，请输入" />
              </div>
            </div>
          </div>
        </div>

        <!-- Step 3: Compliance -->
        <div v-if="activeStep === 2" class="step-panel fade-in">
          <div class="section-card">
            <div class="section-head">
              <h3>尽职调查 (CDD)</h3>
              <span class="sub">根据监管要求，请说明您的账户用途</span>
            </div>
            <div class="form-grid">
              <div class="input-field full">
                <label>资金来源</label>
                <div class="select-wrapper large">
                  <select v-model="form.sourceOfFunds">
                    <option value="" disabled selected>请选择主要资金来源</option>
                    <option value="salary">薪金及佣金</option>
                    <option value="savings">个人积蓄</option>
                    <option value="investment">投资回报</option>
                    <option value="business">商业经营利润</option>
                  </select>
                  <van-icon name="arrow-down" />
                </div>
              </div>
              <div class="input-field full">
                <label>开户目的</label>
                <div class="select-wrapper large">
                  <select v-model="form.openPurpose">
                    <option value="" disabled selected>请选择开户用途</option>
                    <option value="daily">日常消费/储蓄</option>
                    <option value="payroll">接收薪金</option>
                    <option value="invest">理财投资</option>
                    <option value="loan">贷款还款</option>
                  </select>
                  <van-icon name="arrow-down" />
                </div>
              </div>
            </div>
          </div>

          <div class="section-card mt-6 warning-card">
            <div class="section-head">
              <h3>税务声明</h3>
            </div>
            <div class="checkbox-row">
              <input type="checkbox" id="us" v-model="form.isUSPerson" />
              <label for="us">我是美国公民或税务居民 (FATCA)</label>
            </div>
            <p class="warning-text">注：如您是美国纳税人，需填写 W-9 表格。</p>
          </div>
        </div>

        <!-- 底部按钮区 (Moved inside form-content to follow content naturally) -->
        <div class="action-area">
          <button class="btn-primary" @click="activeStep === 2 ? onSubmit() : onNext()" :disabled="loading">
            <van-loading v-if="loading" size="20" />
            <span v-else>{{ activeStep === 2 ? '提交申请' : '下一步' }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 采用垂直布局，允许自然滚动 */
.page-layout {
  min-height: 100vh;
  width: 100%;
  background-color: #F3F4F6;
  display: flex;
  flex-direction: column;
  color: #111827;
}

/* 顶部导航：固定在顶部 */
.header-nav {
  flex-shrink: 0;
  height: 56px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 50;
  border-bottom: 1px solid #E5E7EB;
  box-sizing: border-box;
}
.back-btn { font-size: 20px; color: #374151; cursor: pointer; }
.nav-title { font-size: 16px; font-weight: 700; color: #111827; }
.placeholder { width: 22px; }

/* 滚动区域 */
.scroll-container {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 40px;
  /* 移除 display: flex，回归自然文档流，解决按钮被推到底部的问题 */
}

/* 进度条 */
.progress-section {
  padding: 24px 32px 10px;
  background: #F3F4F6;
  position: relative;
}
.step-indicator {
  display: flex;
  justify-content: space-between;
  position: relative;
  z-index: 2;
}
.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  opacity: 0.5;
  transition: opacity 0.3s;
  flex: 1;
}
.step-item.active { opacity: 1; }

.circle {
  width: 32px; height: 32px;
  border-radius: 50%;
  background: #FFFFFF;
  border: 2px solid #9CA3AF;
  display: flex; align-items: center; justify-content: center;
  font-weight: 800; font-size: 14px; color: #6B7280;
  z-index: 2;
}
.active .circle {
  background: #2563EB;
  border-color: #2563EB;
  color: white;
}
.label { font-size: 12px; font-weight: 600; color: #4B5563; margin-top: 4px; }
.active .label { color: #2563EB; }

.progress-line {
  position: absolute;
  top: 40px; left: 60px; right: 60px;
  height: 2px;
  background: #E5E7EB;
  z-index: 1;
}
.fill { height: 100%; background: #2563EB; transition: width 0.3s ease; }

/* 表单内容 */
.form-content {
  padding: 16px 20px;
  /* 移除 flex: 1 和 display: flex，让内容自然堆叠 */
}

.section-card {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 24px; 
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  margin-bottom: 24px;
}

.section-head { margin-bottom: 20px; border-left: 4px solid #2563EB; padding-left: 12px; }
.section-head h3 { font-size: 16px; font-weight: 700; color: #111827; margin: 0; }
.section-head .sub { font-size: 12px; color: #6B7280; display: block; margin-top: 4px; }

/* Upload Styles - 左右均分布局 */
.upload-row { 
  display: flex; 
  gap: 16px; 
  width: 100%;
}
.upload-box {
  flex: 1; /* 关键：均分宽度，使其分别占据左右半边 */
}

:deep(.van-uploader) { width: 100%; display: flex; justify-content: center; }
:deep(.van-uploader__wrapper) { width: 100%; display: flex; justify-content: center; }
:deep(.van-uploader__upload) { 
  width: 100% !important; 
  height: 110px !important; 
  margin: 0; 
  background: #F9FAFB; 
  border: 2px dashed #D1D5DB;
  border-radius: 12px;
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
  justify-content: center !important;
  transition: all 0.3s;
}
:deep(.van-uploader__upload:active) { background: #EFF6FF; border-color: #2563EB; }

/* 预览图样式优化 */
:deep(.van-uploader__preview) {
  margin: 0;
  width: 100%;
}
:deep(.van-uploader__preview-image) {
  width: 100% !important;
  height: 110px !important;
  border-radius: 12px;
  object-fit: cover;
}
:deep(.van-uploader__mask) {
  display: none;
}

.preview-mask {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 28px;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: white;
  font-size: 10px;
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
}
.preview-mask .van-icon { font-size: 12px; color: #10B981; }

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.skeleton-card {
  position: relative;
  width: 54px;
  height: 36px;
  background: #FFFFFF;
  border: 1.5px solid #E5E7EB;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px; /* 缩小间距，让文字图标更内聚 */
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}

.bg-icon {
  font-size: 22px;
  color: #D1D5DB;
}

.plus-circle {
  position: absolute;
  right: -5px;
  bottom: -5px;
  width: 18px;
  height: 18px;
  background: #2563EB;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 10px;
  border: 1.5px solid #F9FAFB;
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
}

.upload-text {
  font-size: 12px;
  color: #6B7280;
  font-weight: 500;
}

/* Forms - 统一垂直间距为 20px */
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.full { grid-column: span 2; }

.input-field { display: flex; flex-direction: column; gap: 8px; }
.input-field label { font-size: 13px; color: #374151; font-weight: 500; margin-left: 2px; }
.input-field input, .input-field textarea {
  width: 100%;
  background: #F9FAFB;
  border: 1px solid #D1D5DB;
  border-radius: 10px;
  padding: 12px;
  color: #111827;
  font-size: 14px;
  box-sizing: border-box;
  outline: none;
  transition: all 0.2s;
}
.input-field input:focus { border-color: #2563EB; background: white; box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1); }

.select-wrapper {
  position: relative;
  background: #F9FAFB;
  border-radius: 10px;
  height: 46px;
  border: 1px solid #D1D5DB;
}
.select-wrapper.large { height: 50px; }
.select-wrapper select {
  width: 100%; height: 100%;
  background: transparent; border: none; outline: none;
  color: #111827;
  padding: 0 30px 0 12px;
  appearance: none;
  font-size: 14px;
}
.select-wrapper .van-icon {
  position: absolute; right: 12px; top: 50%; transform: translateY(-50%); 
  color: #6B7280; pointer-events: none;
}

/* Radio & Tags */
.radio-group, .region-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.radio-pill, .tag {
  padding: 8px 16px;
  background: #E5E7EB;
  border-radius: 20px;
  font-size: 12px;
  color: #4B5563;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
}
.radio-pill.active, .tag.active {
  background: #EFF6FF;
  color: #2563EB;
  border-color: #2563EB;
  font-weight: 600;
}

.radio-pill.mini {
  padding: 6px 20px;
  flex: 1;
  text-align: center;
}
.radio-group.simple {
  display: flex;
  background: #F9FAFB;
  border: 1px solid #D1D5DB;
  border-radius: 10px;
  padding: 4px;
}

.warning-card { 
  background: #FFFBEB; 
  border: 1px solid #FEF3C7;
  border-left: 4px solid #F59E0B; 
}
.checkbox-row { display: flex; align-items: flex-start; gap: 8px; font-size: 13px; color: #111827; }
.warning-text { font-size: 12px; color: #D97706; margin-top: 8px; }

/* Action Area (Follows content) */
.action-area {
  margin-top: 40px; /* 固定间距，不再使用 auto */
  padding: 0 4px 20px;
}
.btn-primary {
  width: 100%; height: 52px;
  background: #2563EB;
  border: none; border-radius: 14px;
  color: white; font-weight: 600; font-size: 16px;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
  transition: transform 0.2s, background 0.2s;
}
.btn-primary:active { transform: scale(0.98); background: #1D4ED8; }

.mt-6 { margin-top: 24px; }
.fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>
