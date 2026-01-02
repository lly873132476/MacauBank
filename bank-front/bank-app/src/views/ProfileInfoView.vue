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

const profile = ref<any>({})

// 字典映射
const employmentStatusMap: Record<string, string> = {
  '1': '受雇人士',
  '2': '自雇/独资经营',
  '3': '待业/退休',
  '4': '学生'
}

const sourceOfFundsMap: Record<string, string> = {
  'salary': '薪金及佣金',
  'savings': '个人积蓄',
  'investment': '投资回报',
  'business': '商业经营利润'
}

const openPurposeMap: Record<string, string> = {
  'daily': '日常消费/储蓄',
  'payroll': '接收薪金',
  'invest': '理财投资',
  'loan': '贷款还款'
}

const issueOrgsMap: Record<string, string> = {
  'DSI': '身份证明局 (DSI)',
  'CPSP': '治安警察局 (CPSP)',
  'MFA': '中国外交部'
}

// 数据脱敏处理
const maskMobile = (mobile: string) => {
  if (!mobile) return '-'
  return mobile.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const maskIdCard = (id: string) => {
  if (!id) return '-'
  if (id.length <= 4) return id
  return id.substring(0, 2) + '****' + id.substring(id.length - 2)
}

// 格式化日期
const formatDate = (dateStr: string | Date | undefined) => {
  if (!dateStr) return '-'
  try {
    const date = new Date(dateStr)
    return new Intl.DateTimeFormat('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date)
  } catch (e) {
    return dateStr as string
  }
}

// 获取显示文本
const getDisplayText = (value: string | undefined, map: Record<string, string>) => {
  if (!value) return '-'
  return map[value] || value
}

// 页面加载时获取用户信息
onMounted(async () => {
  const result = await userStore.fetchProfile()
  if (result.success && result.data) {
    profile.value = result.data
  } else {
    showToast({
      message: result.message || '获取用户信息失败',
      position: 'top'
    })

    if (result.code && (isAuthError(result.code) || result.code === 102002)) {
      await authStore.logout()
      setTimeout(() => {
        router.replace('/auth')
      }, 1000)
    }
  }
})
</script>

<template>
  <div class="profile-info">
    <div class="profile-info-header">
      <van-nav-bar
        :title="t('profile.personalInfo')"
        left-arrow
        fixed
        @click-left="router.back()"
      />
    </div>

    <div class="profile-info-content">
      <van-cell-group inset title="基本资料">
        <van-cell title="用户名" :value="profile.userName || '-'" icon="user-circle-o" />
        <van-cell title="中文姓名" :value="profile.name || profile.nameCn || '-'" icon="contact-o" />
        <van-cell title="英文姓名" :value="profile.nameEn || '-'" icon="envelop-o" />
        <van-cell title="国籍" :value="profile.nationality || '-'" icon="flag-o" />
        <van-cell title="手机号码" :value="maskMobile(profile.mobile)" icon="phone-o" />
        <van-cell title="身份证号" :value="maskIdCard(profile.idCard)" icon="idcard" />
        <van-cell title="证件有效期" :value="profile.expiryDate || '-'" icon="clock-o" />
        <van-cell title="发证地" :value="profile.issueCountry === 'Macau' ? '中国澳门' : (profile.issueCountry || '-')" icon="location-o" />
        <van-cell title="发证机关" :value="getDisplayText(profile.issueOrg, issueOrgsMap)" icon="hotel-o" />
      </van-cell-group>

      <van-cell-group inset title="职业与居住" class="mt-4">
        <van-cell title="就业状态" :value="getDisplayText(profile.employmentStatus, employmentStatusMap)" icon="manager-o" />
        <van-cell title="职业职位" :value="profile.occupation || '-'" icon="medal-o" />
        <van-cell title="详细地址" :value="profile.addressDetail || '-'" icon="map-marked" />
      </van-cell-group>

      <van-cell-group inset title="合规与账户" class="mt-4">
        <van-cell title="资金来源" :value="getDisplayText(profile.sourceOfFunds, sourceOfFundsMap)" icon="balance-o" />
        <van-cell title="账户状态" icon="shield-o">
          <template #value>
            <van-tag type="primary" plain round v-if="profile.kycLevelDesc">{{ profile.kycLevelDesc }}</van-tag>
            <template v-else>
              <van-tag type="success" plain round v-if="profile.status === 1">正常使用</van-tag>
              <van-tag type="danger" plain round v-else>状态异常</van-tag>
            </template>
          </template>
        </van-cell>
        <van-cell title="注册时间" :value="formatDate(profile.createTime)" icon="calendar-o" />
        <van-cell title="开户目的" :value="getDisplayText(profile.openPurpose, openPurposeMap)" icon="aim" />
        <van-cell title="税务编号" :value="profile.taxId || '-'" icon="description-o" />
      </van-cell-group>

      <div class="footer-tips">
        <van-icon name="info-o" />
        <span>如需修改核心资料，请携带证件前往柜台办理</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-info {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 40px;
}

.profile-info-header {
  position: relative;
  z-index: 100;
}

:deep(.van-nav-bar) {
  background: linear-gradient(135deg, #1989fa, #0570db);
  box-shadow: 0 2px 10px rgba(25, 137, 250, 0.2);
}

:deep(.van-nav-bar__title) {
  color: #ffffff;
  font-weight: 600;
  letter-spacing: 0.5px;
}

:deep(.van-nav-bar .van-icon) {
  color: #ffffff;
}

.profile-info-content {
  padding-top: 60px;
}

:deep(.van-cell-group__title) {
  padding: 16px 16px 8px;
  font-size: 13px;
  color: #969799;
  font-weight: 500;
}

:deep(.van-cell-group--inset) {
  margin: 0 12px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
}

:deep(.van-cell) {
  padding: 14px 16px;
  align-items: center;
}

:deep(.van-cell__title) {
  font-weight: 500;
  color: #323233;
}

:deep(.van-cell__value) {
  color: #646566;
}

:deep(.van-cell__left-icon) {
  font-size: 18px;
  margin-right: 12px;
  color: #1989fa;
}

.mt-4 {
  margin-top: 16px;
}

.footer-tips {
  margin: 30px 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #c8c9cc;
  font-size: 12px;
}
</style>