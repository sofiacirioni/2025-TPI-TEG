import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: !!process.env['CI'],
  retries: process.env['CI'] ? 2 : 0,
  workers: 1,
  reporter: 'html',

  use: {
    baseURL: 'http://localhost:4200',
    headless: false,         // siempre headed para verificar cambios visualmente
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    locale: 'es-AR',
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],

  // Levantar ambos servidores antes de los tests
  webServer: [
    {
      command: 'cd Frontend && npm start',
      url: 'http://localhost:4200',
      reuseExistingServer: true,
      timeout: 120_000,
    },
    {
      command: 'cd Backend && ./mvnw.cmd spring-boot:run',
      url: 'http://localhost:8080/swagger-ui.html',
      reuseExistingServer: true,
      timeout: 180_000,
    },
  ],
});
