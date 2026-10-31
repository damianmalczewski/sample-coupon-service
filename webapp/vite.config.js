import { fileURLToPath, URL } from 'node:url';
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  build: {
    // served by Spring Boot as static resources
    outDir: fileURLToPath(new URL('../src/main/resources/static', import.meta.url)),
    emptyOutDir: true,
  },
  server: {
    proxy: {
      '/api': process.env.API_TARGET ?? 'http://localhost:8080',
    },
  },
});
