import { createRouter, createWebHashHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/home',
      name: 'home',
      component: HomeView
    },
    {
      path: '/account',
      name: 'account',
      component: () => import('../views/AccountView.vue')
    },
    {
      path: '/transfer',
      name: 'transfer',
      component: () => import('../views/TransferView.vue')
    },
    {
      path: '/transfer/records',
      name: 'transfer-records',
      component: () => import('../views/TransferRecordView.vue')
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue')
    },
    {
      path: '/profile/info',
      name: 'profile-info',
      component: () => import('../views/ProfileInfoView.vue')
    },
    {
      path: '/profile/password',
      name: 'profile-password',
      component: () => import('../views/ProfilePasswordView.vue')
    },
    {
      path: '/profile/transaction-password',
      name: 'profile-transaction-password',
      component: () => import('../views/ProfileTransactionPasswordView.vue')
    },
    {
      path: '/profile/settings',
      name: 'profile-settings', // 修改 name 以避免冲突，更具体
      component: () => import('../views/SettingsView.vue')
    },
    {
      path: '/settings', // 新增 /settings 路径
      name: 'settings',
      component: () => import('../views/SettingsView.vue')
    },
    {
      path: '/auth',
      name: 'auth',
      component: () => import('../views/AuthView.vue')
    },
    {
      path: '/payment/qrcode/scan',
      name: 'qrcode-scan',
      component: () => import('../views/QrCodeScanView.vue')
    }
  ]
})

export default router