/**
 * tablero-dev.spec.ts
 * ────────────────────
 * Tests visuales y funcionales del tablero de juego.
 * Reutiliza el estado guardado por tablero-dev.setup.ts (auth + URL).
 *
 * Uso:
 *   npx playwright test                              → setup + todos los tests
 *   npx playwright test --project=chromium          → solo tests (estado previo)
 *   npx playwright test --grep "screenshot"         → solo el screenshot
 *
 * Si el estado expiró: eliminar e2e/.auth/tablero-state.json y volver a correr.
 */

import { test, expect } from '@playwright/test';
import { readFileSync, existsSync } from 'fs';
import path from 'path';

// ── URL del tablero guardada por el setup ──────────────────────────────────
const URL_FILE = path.join(__dirname, '.auth/tablero-url.txt');
const tableroUrl = existsSync(URL_FILE) ? readFileSync(URL_FILE, 'utf-8').trim() : '/juego/';

// Selector de los paths clickeables del mapa:
// Capa 2 tiene stroke="none" estático y [attr.fill] dinámico (no tienen id).
// Capa 2b (bordes) tiene fill="none". El estático (image) no es path.
const PAIS_PATH = 'app-mapa-svg svg path[stroke="none"]';

// ── Ir al tablero antes de cada test ──────────────────────────────────────
test.beforeEach(async ({ page }) => {
  await page.goto(tableroUrl, { waitUntil: 'domcontentloaded' });
  await page.waitForSelector('.tablero-container', { timeout: 20_000 });
  // Esperar a que el mapa haya renderizado al menos un path de país
  await page.waitForSelector(PAIS_PATH, { timeout: 15_000 });
});

// ═══════════════════════════════════════════════════════════════════════════
// VISUAL — layout y estética
// ═══════════════════════════════════════════════════════════════════════════

test.describe('Visual — layout del tablero', () => {

  test('la barra superior de estado es visible', async ({ page }) => {
    await expect(page.locator('.info-barra')).toBeVisible();
  });

  test('el mapa SVG ocupa el espacio central', async ({ page }) => {
    const mapa = page.locator('.mapa-zona');
    await expect(mapa).toBeVisible();
    const box = await mapa.boundingBox();
    const viewport = page.viewportSize()!;
    // El mapa debe ocupar al menos el 55% del ancho de la pantalla
    expect(box!.width).toBeGreaterThan(viewport.width * 0.55);
    // Y al menos el 70% del alto
    expect(box!.height).toBeGreaterThan(viewport.height * 0.70);
  });

  test('el panel de jugadores (derecha) es visible', async ({ page }) => {
    await expect(page.locator('.panel-derecho')).toBeVisible();
    const fichas = page.locator('.ficha-jugador');
    await expect(fichas.first()).toBeVisible();
    expect(await fichas.count()).toBeGreaterThanOrEqual(2);
  });

  test('el reloj vintage es visible en la esquina superior derecha', async ({ page }) => {
    await expect(page.locator('.reloj-container')).toBeVisible();
    // .reloj-face es ahora <object> con el SVG inlineable (agujas animadas via DOM)
    await expect(page.locator('.reloj-face')).toBeVisible();
  });

  test('el sobre del objetivo secreto es visible (top-left)', async ({ page }) => {
    await expect(page.locator('.sobre-objetivo')).toBeVisible();
  });

  test('los paneles laterales NO tienen fondo sólido (política transparente)', async ({ page }) => {
    for (const selector of ['.historial-panel', '.tarjetas-panel', '.chat-panel']) {
      const el = page.locator(selector).first();
      if (await el.isVisible()) {
        const bg = await el.evaluate(e =>
          window.getComputedStyle(e).backgroundColor
        );
        // transparent = rgba(0, 0, 0, 0)
        expect(bg, `${selector} debe ser transparente`).toBe('rgba(0, 0, 0, 0)');
      }
    }
  });

  test('la UI está por encima de la viñeta (z-index > 9999)', async ({ page }) => {
    const elementos: Record<string, string> = {
      '.info-barra':      'barra superior',
      '.panel-derecho':   'panel derecho',
      '.reloj-container': 'reloj',
    };
    for (const [selector, nombre] of Object.entries(elementos)) {
      const el = page.locator(selector).first();
      if (await el.isVisible()) {
        const z = await el.evaluate(e =>
          parseInt(window.getComputedStyle(e).zIndex) || 0
        );
        expect(z, `${nombre} debe estar sobre la viñeta`).toBeGreaterThan(9999);
      }
    }
  });

  test('el fondo es la imagen de la sala de guerra', async ({ page }) => {
    const bg = await page.locator('.tablero-container').evaluate(e =>
      window.getComputedStyle(e).backgroundImage
    );
    expect(bg).toContain('map-scene-wall');
  });

});

// ═══════════════════════════════════════════════════════════════════════════
// MAPA — interacción SVG
// ═══════════════════════════════════════════════════════════════════════════

test.describe('Mapa SVG — renderizado e interacción', () => {

  test('el mapa SVG está renderizado dentro de app-mapa-svg', async ({ page }) => {
    const mapaHost = page.locator('app-mapa-svg');
    await expect(mapaHost).toBeVisible();
    await expect(mapaHost.locator('svg').first()).toBeVisible();
  });

  test('hay al menos 40 países renderizados en el mapa', async ({ page }) => {
    // Los paths clickeables (capa 2) tienen stroke="none" y fill dinámico
    const paths = page.locator(PAIS_PATH);
    const count = await paths.count();
    expect(count).toBeGreaterThan(40);
  });

  test('click en un país abre el modal de acciones', async ({ page }) => {
    const primerPais = page.locator(PAIS_PATH).first();
    await primerPais.click();
    await expect(page.locator('.modal-pais')).toBeVisible({ timeout: 5_000 });
    await expect(page.locator('.modal-nom')).toBeVisible();
  });

  test('el modal de país se cierra al hacer click en cerrar', async ({ page }) => {
    await page.locator(PAIS_PATH).first().click();
    await page.waitForSelector('.modal-pais', { timeout: 5_000 });
    await page.click('.btn-modal-cerrar');
    await expect(page.locator('.modal-pais')).not.toBeVisible({ timeout: 3_000 });
  });

  test('los controles de zoom están presentes', async ({ page }) => {
    await expect(page.locator('.mapa-controles')).toBeVisible();
    await expect(page.locator('.mapa-controles button').nth(0)).toBeVisible();
    await expect(page.locator('.mapa-controles button').nth(1)).toBeVisible();
  });

});

// ═══════════════════════════════════════════════════════════════════════════
// FUNCIONAL — elementos de juego
// ═══════════════════════════════════════════════════════════════════════════

test.describe('Funcional — elementos de juego', () => {

  test('la barra superior muestra información de turno y fase', async ({ page }) => {
    await expect(page.locator('.info-barra .info-lbl').first()).toBeVisible();
    const vals = page.locator('.info-barra .info-val');
    expect(await vals.count()).toBeGreaterThan(0);
  });

  test('el panel de historial existe en el lado izquierdo', async ({ page }) => {
    await expect(page.locator('.panel-izquierdo')).toBeVisible();
    await expect(page.locator('.historial-panel')).toBeVisible();
    await expect(page.locator('.historial-titulo')).toHaveText('REGISTRO DE OPERACIONES');
  });

  test('el sobre del objetivo abre el modal al hacer click', async ({ page }) => {
    await page.click('.sobre-objetivo');
    await expect(page.locator('.objetivo-modal')).toBeVisible({ timeout: 3_000 });
    const texto = await page.locator('.objetivo-texto').textContent();
    expect(texto!.length).toBeGreaterThan(5);
    await page.click('.objetivo-modal');
    await expect(page.locator('.objetivo-modal')).not.toBeVisible({ timeout: 3_000 });
  });

  test('el chat tiene input y botón de envío', async ({ page }) => {
    await expect(page.locator('.chat-panel')).toBeVisible();
    await expect(page.locator('.chat-input')).toBeVisible();
    await expect(page.locator('.chat-send')).toBeVisible();
  });

});

// ═══════════════════════════════════════════════════════════════════════════
// SCREENSHOT — para inspección visual manual
// ═══════════════════════════════════════════════════════════════════════════

test('screenshot: tablero completo', async ({ page }) => {
  await page.waitForTimeout(2_000);
  await page.screenshot({
    path: 'e2e/screenshots/tablero-completo.png',
    fullPage: false,
  });
});

test('screenshot: modal de país', async ({ page }) => {
  await page.locator(PAIS_PATH).first().click();
  await page.waitForSelector('.modal-pais', { timeout: 5_000 });
  await page.waitForTimeout(500);
  await page.screenshot({
    path: 'e2e/screenshots/tablero-modal-pais.png',
    fullPage: false,
  });
});
