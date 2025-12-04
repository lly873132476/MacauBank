import { createI18n } from 'vue-i18n'
import zhCN from '../locales/zh-CN.json'
import zhTW from '../locales/zh-TW.json'

const messages = {
  'zh-CN': zhCN,
  'zh-TW': zhTW
}

const i18n = createI18n({
  legacy: false,
  locale: 'zh-CN',
  fallbackLocale: 'zh-CN',
  messages
})

export default i18n