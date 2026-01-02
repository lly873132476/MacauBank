import { createRouter, createWebHashHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  },
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/home',
      name: 'home',
      component: HomeView,
      meta: { showGlobalNav: true }
    },
    {
      path: '/open-account',
      name: 'open-account',
      component: () => import('../views/OpenAccountView.vue')
    },
    {
      path: '/account',
      name: 'account',
      component: () => import('../views/AccountView.vue'),
      meta: { showGlobalNav: true }
    },
    {
      path: '/transfer',
      name: 'transfer',
      component: () => import('../views/TransferView.vue'),
      meta: { showGlobalNav: true }
    },
    {
      path: '/transfer/records',
      name: 'transfer-records',
      component: () => import('../views/TransferRecordView.vue')
    },
    {
      path: '/transfer/recipient/add',
      name: 'recipient-add',
      component: () => import('../views/RecipientAddView.vue')
    },
    {
      path: '/transfer/recipient/edit/:id',
      name: 'recipient-edit',
      component: () => import('../views/RecipientAddView.vue'),
      props: true
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { showGlobalNav: true }
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
      name: 'profile-settings',
      component: () => import('../views/SettingsView.vue')
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('../views/SettingsView.vue'),
      meta: { showGlobalNav: true }
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