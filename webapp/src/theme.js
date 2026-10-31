import { computed, ref, watchEffect } from 'vue';

// same key and resolution as the inline script in index.html, which applies the theme before first paint
const STORAGE_KEY = 'theme';
const systemDark = window.matchMedia('(prefers-color-scheme: dark)');

function storedPreference() {
  try {
    const value = localStorage.getItem(STORAGE_KEY);
    return value === 'light' || value === 'dark' ? value : 'system';
  } catch {
    return 'system';
  }
}

const systemTheme = ref(systemDark.matches ? 'dark' : 'light');
systemDark.addEventListener('change', () => {
  systemTheme.value = systemDark.matches ? 'dark' : 'light';
});

/** What the user picked: `light`, `dark` or `system` (default, follows the OS setting). */
export const preference = ref(storedPreference());

/** Theme in effect, `light` or `dark`. */
export const theme = computed(() =>
  preference.value === 'system' ? systemTheme.value : preference.value,
);

watchEffect(() => {
  document.documentElement.dataset.theme = theme.value;
});

/**
 * Cycles system, then the system theme set explicitly, then the other one - e.g. on a dark OS: system, dark, light.
 */
export function cyclePreference() {
  const system = systemTheme.value;
  const order = ['system', system, system === 'dark' ? 'light' : 'dark'];
  setPreference(order[(order.indexOf(preference.value) + 1) % order.length]);
}

function setPreference(value) {
  preference.value = value;
  try {
    if (value === 'system') {
      localStorage.removeItem(STORAGE_KEY);
    } else {
      localStorage.setItem(STORAGE_KEY, value);
    }
  } catch {
    // storage unavailable, the choice lasts until reload
  }
}
