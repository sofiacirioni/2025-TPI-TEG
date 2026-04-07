/**
 * tablero-dev.setup.ts
 * ────────────────────
 * Flujo completo: login → crear sala → agregar bots → iniciar partida → tablero.
 * Guarda el estado de autenticación en e2e/.auth/tablero-state.json
 * y la URL del tablero activo en e2e/.auth/tablero-url.txt.
 *
 * Se ejecuta automáticamente antes del proyecto 'chromium' (ver playwright.config.ts).
 * Para forzar un re-setup (estado expirado): eliminar e2e/.auth/tablero-state.json
 */

import { test as setup, expect } from '@playwright/test';
import { writeFileSync, mkdirSync } from 'fs';
import path from 'path';

const AUTH_DIR   = path.join(__dirname, '.auth');
const STATE_FILE = path.join(AUTH_DIR, 'tablero-state.json');
const URL_FILE   = path.join(AUTH_DIR, 'tablero-url.txt');

const CORREO    = 'test@test.com';
const PASSWORD  = 'Test123@';
const SALA_NAME = 'DEV-TABLERO';

setup('setup: login → sala → configPartida → tablero', async ({ page }) => {

  // ── 1. LOGIN ──────────────────────────────────────────────────────────────
  await page.goto('/iniciar-sesion');
  await page.waitForSelector('#nombreUsuario', { timeout: 15_000 });

  await page.fill('#nombreUsuario', CORREO);
  await page.fill('#contrasenia', PASSWORD);
  await page.click('button[type="submit"]');

  // Esperar salida de la pantalla de login
  await page.waitForURL(/\/(principal|entrarCrearSala|juego)/, { timeout: 20_000 });

  // ── 2. NAVEGAR A SALA ─────────────────────────────────────────────────────
  // Ir directo a /entrarCrearSala (localStorage ya tiene la sesión)
  await page.goto('/entrarCrearSala');
  await page.waitForSelector('.tele-opcion', { timeout: 15_000 });

  // Opción A: "Establecer nueva sala de operaciones"
  await page.locator('.tele-opcion').first().click();

  // Esperar que el campo nombre sea visible (CSS .tele-campo-nombre.visible)
  await page.waitForSelector('.tele-campo-nombre.visible .campo-inp', { timeout: 5_000 });
  await page.fill('.tele-campo-nombre .campo-inp', SALA_NAME);

  // Confirmar (botón principal debajo del telegrama)
  await page.locator('.sala-actions .btn-teg-primary').click();
  await page.waitForURL('**/configPartida', { timeout: 15_000 });

  // ── 3. AGREGAR 3 BOTS ─────────────────────────────────────────────────────
  for (let i = 0; i < 3; i++) {
    const slot = page.locator('tr.slot-vacio').first();
    await slot.waitFor({ state: 'visible', timeout: 8_000 });
    await slot.click();
    // Esperar que el slot se convierta en fila de bot (WebSocket actualiza la lista)
    await page.waitForTimeout(1_200);
  }

  // ── 4. INICIAR PARTIDA ────────────────────────────────────────────────────
  const btnIniciar = page.locator('.config-acciones .btn-teg-primary');
  await expect(btnIniciar).not.toBeDisabled({ timeout: 8_000 });
  await btnIniciar.click();

  // Esperar navegación al tablero
  await page.waitForURL('**/juego/**', { timeout: 25_000 });

  // ── 5. ESPERAR CARGA DEL TABLERO ──────────────────────────────────────────
  await page.waitForSelector('.tablero-container', { timeout: 15_000 });
  // Dar tiempo a que el polling inicial cargue la partida
  await page.waitForTimeout(2_000);

  // ── 6. GUARDAR ESTADO ─────────────────────────────────────────────────────
  mkdirSync(AUTH_DIR, { recursive: true });

  // URL del tablero activo (ej: http://localhost:4200/juego/APDA8Z)
  writeFileSync(URL_FILE, page.url(), 'utf-8');

  // Estado de autenticación (cookies + localStorage con JWT/usuario)
  await page.context().storageState({ path: STATE_FILE });

  console.log(`✓ Setup completo. Tablero: ${page.url()}`);
});
