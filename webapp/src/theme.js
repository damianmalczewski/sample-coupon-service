import { ref, watchEffect } from 'vue';

// same key and resolution as the inline script in index.html, which applies the theme before first paint
const STORAGE_KEY = 'theme';
const systemDark = window.matchMedia('(prefers-color-scheme: dark)');

function storedTheme() {
  try {
    const value = localStorage.getItem(STORAGE_KEY);
    return value === 'light' || value === 'dark' ? value : null;
  } catch {
    return null;
  }
}

const systemTheme = () => (systemDark.matches ? 'dark' : 'light');

export const theme = ref(storedTheme() ?? systemTheme());

// follow the system until the user picks a theme
systemDark.addEventListener('change', () => {
  if (storedTheme() === null) {
    theme.value = systemTheme();
  }
});

watchEffect(() => {
  document.documentElement.dataset.theme = theme.value;
});

export function toggleTheme() {
  theme.value = theme.value === 'dark' ? 'light' : 'dark';
  try {
    localStorage.setItem(STORAGE_KEY, theme.value);
  } catch {
    // storage unavailable, the choice lasts until reload
  }
}
