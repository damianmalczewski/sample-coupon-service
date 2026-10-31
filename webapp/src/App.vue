<script setup>
import ThemeSwitch from './components/ThemeSwitch.vue';
</script>

<template>
  <header>
    <nav>
      <RouterLink to="/">Coupons</RouterLink>
      <RouterLink to="/redeem">Redeem</RouterLink>
      <ThemeSwitch />
    </nav>
  </header>
  <main>
    <RouterView />
  </main>
</template>
