import { defineStore } from 'pinia'

export const useUiStore = defineStore('ui', {
  state: () => ({
    isGlobalPopupOpen: false
  }),
  actions: {
    setPopupOpen(status: boolean) {
      this.isGlobalPopupOpen = status
    }
  }
})
