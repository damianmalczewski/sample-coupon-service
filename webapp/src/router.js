import { createRouter, createWebHistory } from 'vue-router'
import CouponsView from './views/CouponsView.vue'
import RedeemView from './views/RedeemView.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: CouponsView },
    { path: '/redeem', component: RedeemView },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})
