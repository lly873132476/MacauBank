<template>
  <div class="recipient-add-page">
    <!-- 顶部导航栏 -->
    <div class="page-header-simple">
      <div class="header-left">
        <div class="back-btn clickable" @click="$router.back()">
          <van-icon name="arrow-left" />
        </div>
        <h1 class="page-title">{{ isEditMode ? '编辑收款人' : '新增收款人' }}</h1>
      </div>
      <div class="header-right">
        <div v-if="isEditMode" class="delete-btn clickable" @click="handleDelete">
          删除
        </div>
      </div>
    </div>

    <!-- 主要内容区 -->
    <div class="main-content">
      <div class="app-card form-card">
        
        <!-- 姓名 -->
        <div class="form-row">
          <span class="row-label">收款人姓名</span>
          <input 
            v-model="form.name" 
            type="text" 
            class="row-input" 
            placeholder="请输入真实姓名" 
          />
        </div>

        <div class="divider-line"></div>

        <!-- 银行选择 -->
        <div class="form-row clickable" @click="showBankPicker = true">
          <span class="row-label">收款银行</span>
          <div class="row-value">
            <span :class="{ 'text-placeholder': !form.bankName }">
              {{ form.bankName || '请选择银行' }}
            </span>
            <van-icon name="arrow" class="arrow-gray" />
          </div>
        </div>

        <div class="divider-line"></div>

        <!-- 账号 -->
        <div class="form-row">
          <span class="row-label">银行账号</span>
          <input 
            v-model="form.account" 
            type="text" 
            class="row-input font-num" 
            placeholder="请输入账号" 
          />
        </div>

        <div class="divider-line"></div>

        <!-- 币种 -->
        <div class="form-row clickable" @click="showCurrencyPicker = true">
          <span class="row-label">默认币种</span>
          <div class="row-value">
            <span>{{ form.currency }}</span>
            <van-icon name="arrow" class="arrow-gray" />
          </div>
        </div>

      </div>

      <!-- 提示文案 -->
      <div class="info-tip">
        <van-icon name="info-o" />
        <span>请确保收款人姓名与银行账户登记的姓名一致，否则可能导致转账失败。</span>
      </div>

      <!-- 底部按钮 -->
      <div class="action-footer">
        <button class="btn-primary btn-block clickable" @click="handleSave" :disabled="loading">
          <van-loading v-if="loading" size="24" color="#fff" />
          <span v-else class="btn-text">{{ isEditMode ? '更新收款人' : '保存收款人' }}</span>
        </button>
      </div>
    </div>

    <!-- 弹窗组件 -->
    <van-popup v-model:show="showBankPicker" position="bottom" round class="glass-popup">
      <van-picker
        title="选择收款银行"
        :columns="bankColumns"
        @confirm="onBankConfirm"
        @cancel="showBankPicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showCurrencyPicker" position="bottom" round class="glass-popup">
      <van-picker
        title="选择默认币种"
        :columns="currencyColumns"
        @confirm="onCurrencyConfirm"
        @cancel="showCurrencyPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { payeeApi } from '../services/api'

const router = useRouter()
const route = useRoute()
const loading = ref(false)

const form = ref({
  id: '',
  name: '',
  bankName: '',
  bankCode: '',
  account: '',
  currency: 'MOP'
})

const isEditMode = computed(() => !!route.params.id)

const showBankPicker = ref(false)
const showCurrencyPicker = ref(false)

const bankColumns = [
  { text: '中国银行澳门分行', value: 'BCMOMO00' },
  { text: '大丰银行', value: 'DMBMMO00' },
  { text: '澳门国际银行', value: 'ZBBAMO00' },
  { text: '澳门商业银行', value: 'CSBMOM00' },
  { text: '工商银行澳门分行', value: 'ICBMMO00' }
]

const currencyColumns = [
  { text: 'MOP', value: 'MOP' },
  { text: 'HKD', value: 'HKD' },
  { text: 'CNY', value: 'CNY' },
  { text: 'USD', value: 'USD' }
]

onMounted(() => {
  if (isEditMode.value) {
    const { id } = route.params
    const { name, bankName, bankCode, account, currency } = route.query
    form.value = {
      id: id as string,
      name: (name as string) || '',
      bankName: (bankName as string) || '',
      bankCode: (bankCode as string) || '',
      account: (account as string) || '',
      currency: (currency as string) || 'MOP'
    }
  }
})

const onBankConfirm = ({ selectedOptions }: any) => {
  form.value.bankName = selectedOptions[0].text
  form.value.bankCode = selectedOptions[0].value
  showBankPicker.value = false
}

const onCurrencyConfirm = ({ selectedOptions }: any) => {
  form.value.currency = selectedOptions[0].value
  showCurrencyPicker.value = false
}

const handleSave = async () => {
  if (!form.value.name) return showToast('请输入姓名')
  if (!form.value.bankName) return showToast('请选择银行')
  if (!form.value.account) return showToast('请输入账号')

  loading.value = true
  
  try {
    let res;
    if (isEditMode.value) {
      res = await payeeApi.update({
        id: form.value.id,
        payeeName: form.value.name,
        accountNo: form.value.account,
        bankCode: form.value.bankCode,
        bankName: form.value.bankName,
        currencyCode: form.value.currency,
        aliasName: form.value.name
      })
    } else {
      res = await payeeApi.add({
        payeeName: form.value.name,
        accountNo: form.value.account,
        bankCode: form.value.bankCode,
        bankName: form.value.bankName,
        currencyCode: form.value.currency,
        aliasName: form.value.name
      })
    }
    
    if (res.code === 200) {
      showToast({ message: isEditMode.value ? '更新成功' : '保存成功', icon: 'success' })
      setTimeout(() => {
        router.back()
      }, 1000)
    } else {
      showToast(res.message || '操作失败')
    }
  } catch (error) {
    showToast('系统繁忙，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleDelete = () => {
  showConfirmDialog({
    title: '确认删除',
    message: '确定要删除该收款人吗？此操作无法撤销。',
    confirmButtonColor: '#EF4444'
  }).then(async () => {
    loading.value = true
    try {
      const res = await payeeApi.delete(form.value.id)
      if (res.code === 200) {
        showToast({ message: '删除成功', icon: 'success' })
        setTimeout(() => {
          router.back()
        }, 1000)
      } else {
        showToast(res.message || '删除失败')
      }
    } catch (error) {
       showToast('操作异常')
    } finally {
      loading.value = false
    }
  }).catch(() => {
    // cancel
  })
}
</script>

<style scoped>
.recipient-add-page {
  min-height: 100vh;
  background-color: #F8F9FA;
  background-image:
    radial-gradient(circle at 10% 20%, rgba(37, 99, 235, 0.04) 0%, transparent 40%),
    radial-gradient(circle at 90% 80%, rgba(236, 72, 153, 0.04) 0%, transparent 40%);
  padding-bottom: 40px;
  max-width: 600px;
  margin: 0 auto;
  font-family: var(--font-body);
}

/* Header */
.page-header-simple {
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(248, 249, 250, 0.8);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-left { display: flex; align-items: center; gap: 16px; }
.back-btn { font-size: 22px; color: var(--text-main); transition: opacity 0.2s; }
.back-btn:active { opacity: 0.6; }
.page-title { font-size: 20px; font-weight: 800; color: var(--text-main); margin: 0; letter-spacing: -0.5px; }   

.delete-btn { color: #EF4444; font-weight: 600; font-size: 14px; padding: 4px 8px; }
.delete-btn:active { opacity: 0.7; }

.main-content { padding: 24px; }

/* Form Card Style */
.form-card {
  padding: 0; /* Let rows handle padding */
  background: white;
  border-radius: 24px;
  box-shadow:
    0 12px 40px -12px rgba(0, 0, 0, 0.08),
    0 2px 12px -4px rgba(0, 0, 0, 0.02),
    0 0 0 1px rgba(0,0,0,0.02);
  overflow: hidden;
}

.form-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 24px;
  font-size: 15px;
  transition: background 0.2s;
  min-height: 24px;
}
.form-row:active { background: #F9FAFB; }

.row-label { 
  color: var(--text-main); 
  font-weight: 600; 
  min-width: 90px; 
}

.row-value { 
  display: flex; 
  align-items: center; 
  gap: 8px; 
  color: var(--text-main); 
  font-weight: 700; 
  justify-content: flex-end; 
  flex: 1; 
}

.row-input {
  border: none; 
  text-align: right; 
  flex: 1;
  font-weight: 700; 
  color: var(--text-main);
  font-size: 16px; 
  outline: none; 
  background: transparent;
}
.row-input::placeholder { color: #D1D5DB; font-weight: 500; }
.text-placeholder { color: #9CA3AF; font-weight: 500; }
.arrow-gray { color: #D1D5DB; font-size: 18px; }

.divider-line { 
  height: 1px; 
  background: #F3F4F6; 
  margin: 0 24px; 
}

/* Info Tip */
.info-tip {
  margin-top: 24px;
  padding: 0 8px;
  display: flex;
  gap: 8px;
  font-size: 13px;
  color: var(--text-sub);
  line-height: 1.5;
  opacity: 0.8;
}
.info-tip .van-icon { font-size: 16px; color: var(--color-primary); margin-top: 2px; }

/* Footer Button */
.action-footer { margin-top: 40px; }
.btn-block {
  width: 100%; height: 60px; border-radius: 30px;
  display: flex; align-items: center; justify-content: center; gap: 10px;
  background: linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%);
  box-shadow:
    0 12px 30px -8px rgba(37, 99, 235, 0.4),
    inset 0 1px 0 rgba(255,255,255,0.2);
  color: white; border: none;
  transition: transform 0.1s;
}
.btn-block:active { transform: scale(0.98); }
.btn-text { font-size: 18px; font-weight: 700; letter-spacing: 0.5px; }
</style>