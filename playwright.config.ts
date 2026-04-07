import { defineConfig, devices } from '@playwright/test';

const AUTH_STATE = 'e2e/.auth/tablero-state.json';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: !!process.env['CI'],
  retries: process.env['CI'] ? 2 : 0,
  workers: 1,
  reporter: 'html',

  use: {
    baseURL: 'http://localhost:4200',
    headless: false,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    locale: 'es-AR',
  },

  projects: [
    // Proyecto setup: corre tablero-dev.setup.ts, no usa storageState guardado
    {
      name: 'setup',
      testMatch: /.*\.setup\.ts/,
    },
    // Proyecto principal: depende de setup, usa el estado guardado
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        storageState: AUTH_STATE,
      },
      dependencies: ['setup'],
    },
  ],

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
