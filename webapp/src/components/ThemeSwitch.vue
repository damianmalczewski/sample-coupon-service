<script setup>
import { computed } from 'vue';
import { cyclePreference, preference, theme } from '../theme.js';

const label = computed(() =>
  preference.value === 'system' ? `Theme: system (${theme.value})` : `Theme: ${preference.value}`,
);
</script>

<template>
  <button
    type="button"
    class="nav-icon"
    :title="label"
    :aria-label="`${label}, click to change`"
    @click="cyclePreference"
  >
    <svg v-if="preference === 'system'" viewBox="0 0 24 24" aria-hidden="true">
      <path
        d="M12 8a2.83 2.83 0 0 0 4 4 4 4 0 1 1-4-4M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M6.3 17.7l-1.4 1.4M19.1 4.9l-1.4 1.4"
      />
    </svg>
    <svg v-else-if="preference === 'light'" viewBox="0 0 24 24" aria-hidden="true">
      <circle cx="12" cy="12" r="4" />
      <path
        d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41"
      />
    </svg>
    <svg v-else viewBox="0 0 24 24" aria-hidden="true">
      <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
    </svg>
  </button>
</template>
