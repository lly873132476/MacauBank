<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const { t } = useI18n()
const router = useRouter()

// 模拟扫码结果
const scanResult = ref('')

// 模拟扫码功能
const startScan = () => {
  // 在实际应用中，这里会调用设备的摄像头进行扫码
  // 这里我们模拟一个扫码结果
  setTimeout(() => {
    scanResult.value = 'https://example.com/payment/12345'
  }, 2000)
}
</script>

<template>
  <div class="qrcode-scan">
    <van-nav-bar
      :title="t('home.scan')"
      left-text="返回"
      left-arrow
      @click-left="() => {router.back()}"
    />
    
    <div class="scan-container">
      <!-- 扫码区域 -->
      <div class="scan-box">
        <van-icon name="scan" class="scan-icon" />
      </div>
      
      <div class="scan-tip">将二维码/条码放入框内，即可自动扫描</div>
      
      <van-button type="primary" round @click="startScan">开始扫描</van-button>
      
      <!-- 扫码结果 -->
      <div v-if="scanResult" class="scan-result">
        <van-cell-group title="扫码结果">
          <van-cell :value="scanResult" />
        </van-cell-group>
        <div style="margin: 16px;">
          <van-button round block type="primary" @click="() => {router.push('/transfer')}">
            去支付
          </van-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.qrcode-scan {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf9 100%);
}

.scan-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
}

.scan-box {
  width: 360px;
  height: 360px;
  border: 5px solid #007bff;
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 44px;
  position: relative;
  box-shadow: 0 12px 32px rgba(0, 123, 255, 0.4);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(240, 245, 255, 0.9) 100%);
}

.scan-box::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 24px;
  box-shadow: inset 0 0 32px rgba(0, 123, 255, 0.7);
}

.scan-icon {
  font-size: 110px;
  color: #007bff;
  text-shadow: 0 4px 8px rgba(0, 123, 255, 0.3);
}

.scan-tip {
  text-align: center;
  margin-bottom: 44px;
  color: #666;
  font-size: 20px;
  font-weight: 600;
}

.scan-result {
  width: 100%;
  max-width: 400px;
}

.van-button {
  font-size: 18px;
  font-weight: 600;
  padding: 16px 28px;
  border-radius: 32px;
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.3);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.van-button:active {
  transform: scale(0.95);
}

.van-cell-group__title {
  font-size: 18px;
  font-weight: 600;
  padding: 14px 18px;
}

.van-cell {
  font-size: 16px;
  font-weight: 500;
}

/* 响应式优化 */
@media (max-width: 767px) {
  .qrcode-scan {
    background: linear-gradient(135deg, #f5f7fa 0%, #e4edf9 100%);
  }
  
  .scan-container {
    padding: 24px;
  }
  
  .scan-box {
    width: 300px;
    height: 300px;
    border: 4px solid #007bff;
    border-radius: 20px;
    margin-bottom: 32px;
  }
  
  .scan-icon {
    font-size: 90px;
  }
  
  .scan-tip {
    font-size: 18px;
    margin-bottom: 32px;
  }
  
  .van-button {
    font-size: 16px;
    padding: 14px 24px;
    border-radius: 28px;
  }
}

@media (max-width: 480px) {
  .scan-container {
    padding: 20px;
  }
  
  .scan-box {
    width: 260px;
    height: 260px;
    border: 3px solid #007bff;
    border-radius: 18px;
    margin-bottom: 28px;
  }
  
  .scan-icon {
    font-size: 80px;
  }
  
  .scan-tip {
    font-size: 16px;
    margin-bottom: 28px;
  }
  
  .van-button {
    font-size: 15px;
    padding: 12px 20px;
    border-radius: 24px;
  }
}
</style>