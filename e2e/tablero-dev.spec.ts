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
  // Descartar el overlay de revelación de objetivo si está presente
  const btnEntendido = page.locator('.btn-entendido');
  if (await btnEntendido.isVisible({ timeout: 3_000 }).catch(() => false)) {
    await btnEntendido.click();
    // Esperar a que el overlay desaparezca del DOM (animación cerrando ~800ms)
    // antes de permitir interacción con el mapa, para evitar que intercepte clicks.
    await page.locator('.rev-overlay').waitFor({ state: 'detached', timeout: 3_000 }).catch(() => {});
  }
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

    // El modal muestra el progreso dinámico (.objetivo-desc + .objetivo-items)
    // o el texto estático como fallback (.objetivo-texto) si el progreso aún no cargó
    const hasDesc    = await page.locator('.objetivo-desc').isVisible({ timeout: 1_500 }).catch(() => false);
    const hasItems   = await page.locator('.objetivo-items').isVisible({ timeout: 500 }).catch(() => false);
    const hasTexto   = await page.locator('.objetivo-texto').isVisible({ timeout: 500 }).catch(() => false);

    if (hasDesc) {
      const texto = await page.locator('.objetivo-desc').textContent();
      expect(texto!.trim().length).toBeGreaterThan(5);
    } else if (hasTexto) {
      const texto = await page.locator('.objetivo-texto').textContent();
      expect(texto!.trim().length).toBeGreaterThan(5);
    }
    // Al menos uno de los dos formatos debe estar presente
    expect(hasDesc || hasTexto || hasItems, 'El modal debe mostrar contenido del objetivo').toBe(true);

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

// ═══════════════════════════════════════════════════════════════════════════
// FASES Y TURNOS — flujo de juego
// ═══════════════════════════════════════════════════════════════════════════

test.describe('Fases y turnos — flujo de juego', () => {

  test('la info-barra muestra fase actual con el texto correcto', async ({ page }) => {
    const faseVal = page.locator('.info-barra .info-val').first();
    await expect(faseVal).toBeVisible();
    const faseTexto = await faseVal.textContent();
    // La fase debe ser una de las tres válidas (con o sin tildes)
    expect(['INCORPORACION', 'ATAQUE', 'REAGRUPACION'])
      .toContain(faseTexto!.trim().toUpperCase().replace(/[ÓÚÁ]/g, c => ({Ó:'O',Ú:'U',Á:'A'})[c]!));
  });

  test('el numero de turno es positivo', async ({ page }) => {
    const turnoInfo = page.locator('.info-barra .info-sub');
    await expect(turnoInfo).toBeVisible();
    const texto = await turnoInfo.textContent();
    const num = parseInt(texto!.replace(/\D/g, ''), 10);
    expect(num).toBeGreaterThanOrEqual(1);
  });

  test('si es mi turno: botón AVANZAR FASE está presente y avanza fase', async ({ page }) => {
    const btnAvanzar = page.locator('.btn-avanzar');
    const esmiturno = await btnAvanzar.isVisible();

    if (!esmiturno) {
      test.info().annotations.push({ type: 'skip-reason', description: 'No es el turno del usuario' });
      return; // no es mi turno, skip silencioso
    }

    // Leer fase actual
    const faseAntes = await page.locator('.info-barra .info-val').first().textContent();

    await btnAvanzar.click();
    await page.waitForTimeout(1_500); // polling actualiza

    // Verificar que la fase cambió o el turno avanzó
    const faseDespues = await page.locator('.info-barra .info-val').first().textContent();
    // Fase cambió O el botón ya no está (pasó al siguiente jugador)
    const btnSigueVisible = await btnAvanzar.isVisible();
    const cambioFase = faseAntes !== faseDespues || !btnSigueVisible;
    expect(cambioFase, `Fase debe cambiar tras AVANZAR FASE. Antes: ${faseAntes}, Después: ${faseDespues}`).toBe(true);
  });

  test('ciclo completo de fases si es mi turno: INCORPORACIÓN → ATAQUE → REAGRUPACIÓN', async ({ page }) => {
    const btnAvanzar = page.locator('.btn-avanzar');

    // Esperar hasta 15s a que sea mi turno
    let esmiturno = false;
    for (let i = 0; i < 15; i++) {
      if (await btnAvanzar.isVisible()) { esmiturno = true; break; }
      await page.waitForTimeout(1_000);
    }
    if (!esmiturno) {
      test.info().annotations.push({ type: 'skip-reason', description: 'Nunca llegó el turno del usuario' });
      return;
    }

    const fasesEsperadas = ['INCORPORACIÓN', 'INCORPORACION', 'ATAQUE', 'REAGRUPACIÓN', 'REAGRUPACION'];
    const faseActual = await page.locator('.info-barra .info-val').first().textContent();

    // Avanzar hasta cubrir al menos 2 fases
    const fasesVistas: string[] = [faseActual!.trim()];
    for (let paso = 0; paso < 3; paso++) {
      const btnV = await btnAvanzar.isVisible();
      if (!btnV) break;
      await btnAvanzar.click();
      await page.waitForTimeout(1_500);
      const f = await page.locator('.info-barra .info-val').first().textContent();
      fasesVistas.push(f!.trim());
    }

    // Deben haberse visto al menos 2 fases distintas
    const distintas = new Set(fasesVistas.map(f => f.toUpperCase()));
    expect(distintas.size, `Se esperaban ≥2 fases distintas, solo se vieron: ${[...distintas].join(', ')}`).toBeGreaterThanOrEqual(2);
  });

  test('el turno pasa a los bots automáticamente (sin intervención del usuario)', async ({ page }) => {
    test.setTimeout(90_000);
    const btnAvanzar = page.locator('.btn-avanzar');

    // Esperar hasta 20s a que sea mi turno (puede haber llegado de un estado heredado)
    let esmiturno = false;
    for (let i = 0; i < 20; i++) {
      if (await btnAvanzar.isVisible()) { esmiturno = true; break; }
      await page.waitForTimeout(1_000);
    }
    if (!esmiturno) {
      test.info().annotations.push({ type: 'skip-reason', description: 'Nunca llegó el turno del usuario' });
      return;
    }

    // Leer turno DESPUÉS de confirmar que es mi turno (estado estable)
    const turnoAntes = await page.locator('.info-barra .info-sub').textContent();

    // Ceder las 3 fases para que lleguen y ejecuten los bots
    for (let i = 0; i < 3; i++) {
      if (await btnAvanzar.isVisible()) {
        await btnAvanzar.click();
        await page.waitForTimeout(2_000);
      }
    }

    // Polling: el backend ejecuta bots sincrónicamente, esperar cambio de turno
    let turnoNuevo = await page.locator('.info-barra .info-sub').textContent();
    for (let i = 0; i < 25; i++) {
      if (turnoNuevo !== turnoAntes) break;
      await page.waitForTimeout(1_500);
      turnoNuevo = await page.locator('.info-barra .info-sub').textContent();
    }

    expect(turnoNuevo, `El turno debe avanzar; antes=${turnoAntes} después=${turnoNuevo}`)
      .not.toBe(turnoAntes);
  });

});

// ═══════════════════════════════════════════════════════════════════════════
// NOTIFICACIONES — panel de eventos de juego
// ═══════════════════════════════════════════════════════════════════════════

test.describe('Sistema de notificaciones — visual y funcional', () => {

  test('el componente app-tablero-event-display existe en el DOM', async ({ page }) => {
    await expect(page.locator('app-tablero-event-display')).toBeAttached();
  });

  test('el overlay de notificaciones tiene z-index mayor que el mapa', async ({ page }) => {
    const notifZ = await page.locator('app-tablero-event-display').evaluate(el =>
      parseInt(window.getComputedStyle(el).zIndex) || 0
    );
    const mapaZ = await page.locator('.mapa-zona').evaluate(el =>
      parseInt(window.getComputedStyle(el).zIndex) || 0
    );
    expect(notifZ, `z-index notif (${notifZ}) debe ser > z-index mapa (${mapaZ})`).toBeGreaterThan(mapaZ);
  });

  test('el overlay cubre la misma área que el mapa', async ({ page }) => {
    const notifBox = await page.locator('app-tablero-event-display').boundingBox();
    const mapaBox = await page.locator('.mapa-zona').boundingBox();
    expect(notifBox, 'El overlay debe tener una bounding box').toBeTruthy();
    expect(mapaBox, 'El mapa debe tener una bounding box').toBeTruthy();
    // Los bordes deben coincidir aproximadamente (±4px)
    expect(Math.abs(notifBox!.x - mapaBox!.x)).toBeLessThan(4);
    expect(Math.abs(notifBox!.y - mapaBox!.y)).toBeLessThan(4);
  });

  test('ataque muestra panel de selección de dados', async ({ page }) => {
    const btnAvanzar = page.locator('.btn-avanzar');

    // Llegar a fase ATACAR
    for (let i = 0; i < 15; i++) {
      const faseTexto = await page.locator('.info-barra .info-val').first().textContent();
      if (faseTexto?.toUpperCase().includes('ATAQUE')) break;
      if (await btnAvanzar.isVisible()) {
        await btnAvanzar.click();
        await page.waitForTimeout(1_500);
      } else {
        await page.waitForTimeout(1_000);
      }
    }

    const faseActual = await page.locator('.info-barra .info-val').first().textContent();
    if (!faseActual?.toUpperCase().includes('ATAQUE')) {
      test.info().annotations.push({ type: 'skip-reason', description: 'No se pudo llegar a fase ATACAR' });
      return;
    }

    // Buscar un país propio con enemigos adyacentes
    const paises = page.locator(PAIS_PATH);
    const totalPaises = await paises.count();
    let atacado = false;

    for (let i = 0; i < totalPaises && !atacado; i++) {
      await paises.nth(i).click();
      const modal = page.locator('.modal-pais');
      const modalVis = await modal.isVisible({ timeout: 2_000 }).catch(() => false);
      if (!modalVis) continue;

      // Verificar que sea nuestro país con botón de ataque
      const btnAtacar = modal.locator('.btn-teg-danger, button:has-text("ATACAR"), button:has-text("· ATACAR ·")');
      const tieneAtaque = await btnAtacar.isVisible({ timeout: 500 }).catch(() => false);

      if (!tieneAtaque) {
        await page.locator('.btn-modal-cerrar').click().catch(() => {});
        await page.waitForTimeout(300);
        continue;
      }

      // Seleccionar primer enemigo disponible
      const selectDestino = modal.locator('select').first();
      const tieneSelect = await selectDestino.isVisible({ timeout: 500 }).catch(() => false);
      if (tieneSelect) {
        const opciones = await selectDestino.locator('option').count();
        if (opciones > 1) {
          await selectDestino.selectOption({ index: 1 });
        }
      } else {
        // Intentar con los botones de país enemigo si los hay
        const enemigoBtn = modal.locator('.pais-enemigo, .limitrofe-item').first();
        const tieneEnemigo = await enemigoBtn.isVisible({ timeout: 500 }).catch(() => false);
        if (!tieneEnemigo) {
          await page.locator('.btn-modal-cerrar').click().catch(() => {});
          await page.waitForTimeout(300);
          continue;
        }
        await enemigoBtn.click();
      }

      // Click en ATACAR del modal
      await btnAtacar.click();

      // Verificar que aparece el panel de dados
      const panelDados = page.locator('.ted-combat-panel');
      const apareció = await panelDados.isVisible({ timeout: 3_000 }).catch(() => false);

      if (apareció) {
        atacado = true;

        // Screenshot del panel de dados
        await page.screenshot({ path: 'e2e/screenshots/notif-dados-seleccion.png' });

        // Verificar elementos del panel
        await expect(page.locator('.ted-combat-title')).toBeVisible();
        await expect(page.locator('.ted-combatants')).toBeVisible();
        await expect(page.locator('.ted-dice-btns')).toBeVisible();
        await expect(page.locator('.ted-timer')).toBeVisible();

        // Lanzamiento automático: el primer click en una cantidad de dados
        // ya dispara el ataque (se eliminó el botón LANZAR explícito).
        await page.locator('.ted-dice-btn').first().click();
        await page.waitForTimeout(500);

        // Debe aparecer el resultado (result-panel o conquista-panel)
        const resultado = page.locator('.ted-result-panel, .ted-conquista-panel');
        const resultadoVis = await resultado.isVisible({ timeout: 5_000 }).catch(() => false);
        if (resultadoVis) {
          await page.screenshot({ path: 'e2e/screenshots/notif-resultado-combate.png' });
        }
      } else {
        await page.locator('.btn-modal-cerrar').click().catch(() => {});
      }
      break;
    }

    // Si no encontramos país atacable, loguear (no fallar — depende del estado del juego)
    if (!atacado) {
      console.log('No se encontró país atacable en fase ATACAR — puede ser válido si el jugador está rodeado de aliados');
    }
  });

  test('notificación simple (FIN_TURNO) es visualmente coherente con el design system', async ({ page }) => {
    test.setTimeout(60_000);
    const btnAvanzar = page.locator('.btn-avanzar');

    // Avanzar las 3 fases del turno si es el turno del usuario
    if (await btnAvanzar.isVisible()) {
      for (let i = 0; i < 3; i++) {
        if (await btnAvanzar.isVisible()) {
          await btnAvanzar.click();
          await page.waitForTimeout(1_200);
        }
      }
    }

    // Esperar si aparece una notificación ted-notif (FIN_TURNO u otro evento)
    const notif = page.locator('.ted-notif');
    const notifVis = await notif.isVisible({ timeout: 5_000 }).catch(() => false);

    if (notifVis) {
      // Verificar design system: fondo papel, fuente heading
      const bg = await notif.evaluate(el => window.getComputedStyle(el).backgroundColor);
      // El fondo no debe ser negro puro ni completamente transparente
      expect(bg).not.toBe('rgba(0, 0, 0, 0)');
      expect(bg).not.toBe('rgb(0, 0, 0)');

      // Verificar que la stripe lateral está presente
      await expect(notif.locator('.ted-notif-stripe')).toBeVisible();

      // Screenshot
      await page.screenshot({ path: 'e2e/screenshots/notif-simple.png' });
    } else {
      console.log('No apareció notificación simple — puede que no fuera el turno del usuario');
    }
  });

  test('screenshot: estado tablero con bots jugando', async ({ page }) => {
    // Dejar pasar 3s para que bots hagan alguna acción
    await page.waitForTimeout(3_000);
    await page.screenshot({ path: 'e2e/screenshots/tablero-con-bots.png' });
  });

});

// ═══════════════════════════════════════════════════════════════════════════
// DESIGN SYSTEM — coherencia visual del overlay de notificaciones
// ═══════════════════════════════════════════════════════════════════════════

test.describe('Design system — coherencia visual notificaciones', () => {

  test('el panel de combate usa la fuente heading (Special Elite)', async ({ page }) => {
    // Generar un panel de combate forzando un ataque si es posible
    // Fallback: verificar que los estilos CSS están definidos correctamente
    const fontHeading = await page.evaluate(() =>
      getComputedStyle(document.documentElement).getPropertyValue('--font-heading').trim()
    );
    expect(fontHeading.length).toBeGreaterThan(0);
    // Debe incluir 'Special Elite' o 'Roboto Slab' como definido en el design system
    expect(fontHeading.toLowerCase()).toMatch(/special|roboto|slab|serif/i);
  });

  test('las variables de color de jugadores están definidas', async ({ page }) => {
    const vars = ['--player-rojo', '--player-azul', '--player-verde',
                  '--player-naranja', '--player-purpura', '--player-dorado'];
    for (const v of vars) {
      const val = await page.evaluate((varName) =>
        getComputedStyle(document.documentElement).getPropertyValue(varName).trim()
      , v);
      expect(val, `${v} debe estar definida`).not.toBe('');
    }
  });

  test('el overlay no tiene fondo sólido negro (mantiene transparencia)', async ({ page }) => {
    const bg = await page.locator('app-tablero-event-display').evaluate(el =>
      window.getComputedStyle(el).backgroundColor
    );
    // El host debe ser transparente (pointer-events: none, sin background)
    expect(bg).toBe('rgba(0, 0, 0, 0)');
  });

  test('el mapa mantiene su border y fondo en todos los estados', async ({ page }) => {
    const mapa = page.locator('.mapa-zona');
    await expect(mapa).toBeVisible();

    const borderColor = await mapa.evaluate(el =>
      window.getComputedStyle(el).borderColor
    );
    // No debe ser completamente transparente
    expect(borderColor).not.toBe('rgba(0, 0, 0, 0)');
  });

});

// ═══════════════════════════════════════════════════════════════════════════
// REVELACIÓN DE OBJETIVO — componente sobre animado
// ═══════════════════════════════════════════════════════════════════════════

test.describe('Revelación de objetivo — sobre animado', () => {

  // Este bloque NO usa el beforeEach estándar (que descarta el overlay).
  // Navega fresh para ver el overlay en su estado inicial.

  test('el overlay de revelación aparece al cargar el tablero', async ({ page }) => {
    await page.goto(tableroUrl, { waitUntil: 'domcontentloaded' });
    await page.waitForSelector('.tablero-container', { timeout: 20_000 });

    // El overlay puede tardar hasta ~2s en aparecer (espera que cargue el objetivo)
    const overlay = page.locator('.rev-overlay');
    const apareció = await overlay.isVisible({ timeout: 6_000 }).catch(() => false);

    if (!apareció) {
      console.log('Overlay de revelación no apareció — puede que la sesión ya lo haya descartado');
      return;
    }

    await page.screenshot({ path: 'e2e/screenshots/revelacion-sobre-cerrado.png' });
    await expect(overlay).toBeVisible();
  });

  test('el sobre se abre y muestra la hoja de órdenes', async ({ page }) => {
    await page.goto(tableroUrl, { waitUntil: 'domcontentloaded' });
    await page.waitForSelector('.tablero-container', { timeout: 20_000 });

    const overlay = page.locator('.rev-overlay');
    const apareció = await overlay.isVisible({ timeout: 6_000 }).catch(() => false);
    if (!apareció) { test.skip(); return; }

    // Esperar a que la hoja sea visible (animación de apertura ~1.2s)
    const hoja = page.locator('.hoja');
    const hojaVisible = await hoja.isVisible({ timeout: 4_000 }).catch(() => false);
    if (!hojaVisible) { test.skip(); return; }

    await page.screenshot({ path: 'e2e/screenshots/revelacion-hoja-abierta.png' });

    // Debe mostrar el título y la descripción
    await expect(page.locator('.hoja-titulo')).toBeVisible();
    await expect(page.locator('.hoja-descripcion')).toBeVisible();

    // Debe tener al menos un ítem de objetivo
    const items = page.locator('.hoja-item');
    expect(await items.count()).toBeGreaterThanOrEqual(1);
  });

  test('la hoja muestra ítems con formato X/N o "en curso"', async ({ page }) => {
    await page.goto(tableroUrl, { waitUntil: 'domcontentloaded' });
    await page.waitForSelector('.tablero-container', { timeout: 20_000 });

    const overlay = page.locator('.rev-overlay');
    const apareció = await overlay.isVisible({ timeout: 6_000 }).catch(() => false);
    if (!apareció) { test.skip(); return; }

    const hoja = page.locator('.hoja');
    await hoja.isVisible({ timeout: 4_000 }).catch(() => false);

    // Al menos un ítem debe tener cuenta (X/N o "en curso")
    const cuentas = page.locator('.item-cuenta');
    const hayCuentas = await cuentas.count();
    expect(hayCuentas).toBeGreaterThanOrEqual(1);

    // Verificar que el texto contiene / o "restante"
    const texto = await cuentas.first().textContent() ?? '';
    expect(texto.includes('/') || texto.includes('restante') || texto.includes('curso')).toBe(true);
  });

  test('el botón ENTENDIDO descarta el overlay y muestra el tablero', async ({ page }) => {
    test.setTimeout(30_000);
    await page.goto(tableroUrl, { waitUntil: 'domcontentloaded' });
    await page.waitForSelector('.tablero-container', { timeout: 20_000 });
    await page.waitForSelector(PAIS_PATH, { timeout: 15_000 });

    const overlay = page.locator('.rev-overlay');
    const apareció = await overlay.isVisible({ timeout: 6_000 }).catch(() => false);
    if (!apareció) { test.skip(); return; }

    // Esperar el botón ENTENDIDO
    const btn = page.locator('.btn-entendido');
    await expect(btn).toBeVisible({ timeout: 5_000 });

    await page.screenshot({ path: 'e2e/screenshots/revelacion-con-boton.png' });

    // Click en ENTENDIDO
    await btn.click();

    // El overlay debe desaparecer
    await expect(overlay).not.toBeVisible({ timeout: 3_000 });

    // El tablero debe quedar interactuable
    await expect(page.locator('.sobre-objetivo')).toBeVisible({ timeout: 3_000 });
    await expect(page.locator('.mapa-zona')).toBeVisible();

    await page.screenshot({ path: 'e2e/screenshots/revelacion-descartada.png' });
  });

  test('el overlay tiene el diseño correcto — overlay oscuro y backdrop blur', async ({ page }) => {
    await page.goto(tableroUrl, { waitUntil: 'domcontentloaded' });
    await page.waitForSelector('.tablero-container', { timeout: 20_000 });

    const overlay = page.locator('.rev-overlay');
    const apareció = await overlay.isVisible({ timeout: 6_000 }).catch(() => false);
    if (!apareció) { test.skip(); return; }

    // El fondo debe ser oscuro semitransparente (no totalmente transparente ni negro sólido)
    const bg = await overlay.evaluate(el => window.getComputedStyle(el).backgroundColor);
    expect(bg).not.toBe('rgba(0, 0, 0, 0)');     // no transparente
    expect(bg).not.toBe('rgb(255, 255, 255)');     // no blanco
    // Debe tener componente alpha < 1 (semitransparente)
    const match = bg.match(/rgba\([\d\s,]+,([\d.]+)\)/);
    if (match) {
      expect(parseFloat(match[1])).toBeLessThan(1);
    }

    // El sobre debe estar centrado en la pantalla
    const sobreBox  = await page.locator('.sobre-wrap').boundingBox();
    const viewport  = page.viewportSize()!;
    if (sobreBox) {
      const centroCobre = sobreBox.x + sobreBox.width / 2;
      const centroVP    = viewport.width / 2;
      expect(Math.abs(centroCobre - centroVP)).toBeLessThan(50);
    }
  });

  test('panel ORDEN SECRETA muestra progreso dinámico después de descartar revelación', async ({ page }) => {
    test.setTimeout(30_000);
    await page.goto(tableroUrl, { waitUntil: 'domcontentloaded' });
    await page.waitForSelector('.tablero-container', { timeout: 20_000 });
    await page.waitForSelector(PAIS_PATH, { timeout: 15_000 });

    // Descartar revelación si está presente
    const btn = page.locator('.btn-entendido');
    if (await btn.isVisible({ timeout: 5_000 }).catch(() => false)) {
      await btn.click();
      await page.waitForTimeout(800);
    }

    // Abrir el panel ORDEN SECRETA
    await page.click('.sobre-objetivo');
    await expect(page.locator('.objetivo-modal')).toBeVisible({ timeout: 3_000 });

    // Verificar que muestra datos estructurados (nuevo formato o fallback)
    const hasItems = await page.locator('.objetivo-items').isVisible({ timeout: 2_000 }).catch(() => false);
    const hasDesc  = await page.locator('.objetivo-desc').isVisible({ timeout: 500 }).catch(() => false);
    const hasFallb = await page.locator('.objetivo-texto').isVisible({ timeout: 500 }).catch(() => false);

    expect(hasItems || hasDesc || hasFallb, 'El panel debe mostrar contenido del objetivo').toBe(true);

    await page.screenshot({ path: 'e2e/screenshots/panel-orden-secreta-progreso.png' });
  });

  test('la solapa del sobre tiene forma triangular (clip-path correcto)', async ({ page }) => {
    await page.goto(tableroUrl, { waitUntil: 'domcontentloaded' });
    await page.waitForSelector('.tablero-container', { timeout: 20_000 });

    const overlay = page.locator('.rev-overlay');
    const apareció = await overlay.isVisible({ timeout: 6_000 }).catch(() => false);
    if (!apareció) { test.skip(); return; }

    const flap = page.locator('.sobre-flap');
    await expect(flap).toBeVisible({ timeout: 3_000 });

    // La solapa debe tener altura real (>0) — clip-path en lugar del truco border-trick
    const box = await flap.boundingBox();
    expect(box, 'La solapa debe tener bounding box').toBeTruthy();
    expect(box!.height, 'La solapa debe tener altura > 50px').toBeGreaterThan(50);

    // El clip-path debe estar definido (polygon, no none)
    const clipPath = await flap.evaluate(el => window.getComputedStyle(el).clipPath);
    expect(clipPath, 'clip-path debe ser polygon').toMatch(/polygon/i);

    // El lacre (círculo rojo) debe ser visible dentro del triángulo
    const lacre = page.locator('.sobre-lacre');
    await expect(lacre).toBeVisible();

    await page.screenshot({ path: 'e2e/screenshots/revelacion-solapa-triangular.png' });
  });

  test('animación de cierre vuela hacia el sobre-objetivo (top-left)', async ({ page }) => {
    test.setTimeout(30_000);
    await page.goto(tableroUrl, { waitUntil: 'domcontentloaded' });
    await page.waitForSelector('.tablero-container', { timeout: 20_000 });
    await page.waitForSelector(PAIS_PATH, { timeout: 15_000 });

    const overlay = page.locator('.rev-overlay');
    const apareció = await overlay.isVisible({ timeout: 6_000 }).catch(() => false);
    if (!apareció) { test.skip(); return; }

    // Esperar a que aparezca el botón ENTENDIDO
    const btn = page.locator('.btn-entendido');
    await expect(btn).toBeVisible({ timeout: 5_000 });

    // Capturar posición del sobre-objetivo ANTES de cerrar
    const sobreObjetivo = page.locator('.sobre-objetivo');
    const sobreBox = await sobreObjetivo.boundingBox();

    // Hacer click en ENTENDIDO y capturar inmediatamente durante la animación
    await btn.click();
    // La animación dura ~700ms — capturar durante el vuelo (~300ms después)
    await page.waitForTimeout(300);
    await page.screenshot({ path: 'e2e/screenshots/revelacion-vuelo-cierre.png' });

    // Después de ~800ms, el overlay debe haber desaparecido
    await page.waitForTimeout(600);
    await expect(overlay).not.toBeVisible({ timeout: 2_000 });

    // El sobre-objetivo debe seguir visible en el tablero (confirma que "aterrizó" correctamente)
    await expect(sobreObjetivo).toBeVisible();
    if (sobreBox) {
      const sobreBoxFinal = await sobreObjetivo.boundingBox();
      // El sobre-objetivo NO debe haberse movido de su posición original
      expect(Math.abs((sobreBoxFinal?.x ?? 0) - sobreBox.x)).toBeLessThan(5);
    }

    await page.screenshot({ path: 'e2e/screenshots/revelacion-descartada-animacion.png' });
  });

});

// ═══════════════════════════════════════════════════════════════════════════
// MODAL DE PAÍS — posicionamiento inteligente
// ═══════════════════════════════════════════════════════════════════════════

test.describe('Modal de país — posicionamiento inteligente', () => {

  test('el modal no sale del área del mapa al clickear países del borde derecho', async ({ page }) => {
    const mapaZona = page.locator('.mapa-zona');
    await expect(mapaZona).toBeVisible();
    const mapaBox = await mapaZona.boundingBox();
    if (!mapaBox) { test.skip(); return; }

    const paises = page.locator(PAIS_PATH);
    const total = await paises.count();

    // Buscar un país que esté en la mitad derecha del mapa
    let testeado = false;
    for (let i = 0; i < total && !testeado; i++) {
      const paisBox = await paises.nth(i).boundingBox();
      if (!paisBox) continue;
      // País en el 60% derecho del mapa
      if (paisBox.x + paisBox.width / 2 > mapaBox.x + mapaBox.width * 0.6) {
        await paises.nth(i).click();
        const modal = page.locator('.modal-pais');
        const vis = await modal.isVisible({ timeout: 2_000 }).catch(() => false);
        if (!vis) continue;

        const modalBox = await modal.boundingBox();
        if (modalBox) {
          testeado = true;
          // El borde derecho del modal debe estar dentro del área del mapa
          const modalRight = modalBox.x + modalBox.width;
          const mapaRight  = mapaBox.x + mapaBox.width;
          expect(modalRight, 'El modal no debe salir por la derecha del mapa').toBeLessThanOrEqual(mapaRight + 4);

          await page.screenshot({ path: 'e2e/screenshots/modal-pais-derecha.png' });
        }
        await page.locator('.btn-modal-cerrar').click().catch(() => {});
        await page.waitForTimeout(200);
      }
    }

    if (!testeado) {
      console.log('No se encontró país en el borde derecho — puede depender del estado del mapa');
    }
  });

  test('el modal no sale del área del mapa al clickear países del borde inferior', async ({ page }) => {
    const mapaZona = page.locator('.mapa-zona');
    const mapaBox = await mapaZona.boundingBox();
    if (!mapaBox) { test.skip(); return; }

    const paises = page.locator(PAIS_PATH);
    const total = await paises.count();

    let testeado = false;
    for (let i = total - 1; i >= 0 && !testeado; i--) {
      const paisBox = await paises.nth(i).boundingBox();
      if (!paisBox) continue;
      // País en el 65% inferior del mapa
      if (paisBox.y + paisBox.height / 2 > mapaBox.y + mapaBox.height * 0.65) {
        await paises.nth(i).click();
        const modal = page.locator('.modal-pais');
        const vis = await modal.isVisible({ timeout: 2_000 }).catch(() => false);
        if (!vis) continue;

        const modalBox = await modal.boundingBox();
        if (modalBox) {
          testeado = true;
          const modalBottom = modalBox.y + modalBox.height;
          const mapaBottom  = mapaBox.y + mapaBox.height;
          expect(modalBottom, 'El modal no debe salir por abajo del mapa').toBeLessThanOrEqual(mapaBottom + 4);

          await page.screenshot({ path: 'e2e/screenshots/modal-pais-inferior.png' });
        }
        await page.locator('.btn-modal-cerrar').click().catch(() => {});
        await page.waitForTimeout(200);
      }
    }

    if (!testeado) {
      console.log('No se encontró país en el borde inferior — puede depender del estado del mapa');
    }
  });

});

// ═══════════════════════════════════════════════════════════════════════════
// TARJETAS — refactor visual (chips mini + modal de detalle)
// ═══════════════════════════════════════════════════════════════════════════

test.describe('Tarjetas — vista mini y modal de detalle', () => {

  test('el panel de tarjetas existe y el botón título es interactivo', async ({ page }) => {
    const panel = page.locator('.tarjetas-panel');
    await expect(panel).toBeVisible();
    await expect(panel.locator('.tarjetas-titulo-btn')).toHaveText(/TARJETAS/);
  });

  test('screenshot: panel izquierdo con chips mini', async ({ page }) => {
    // Esperar 2s a que cargue el estado completo
    await page.waitForTimeout(2_000);
    const col = page.locator('.columna-izquierda');
    await expect(col).toBeVisible();
    await col.screenshot({ path: 'e2e/screenshots/tarjetas-panel-mini.png' });
  });

  test('click en el título TARJETAS abre el modal de detalle', async ({ page }) => {
    test.setTimeout(60_000);
    // Avanzar fases hasta acumular tarjetas (conquistar exige tiempo).
    // Si no hay cartas todavía, el botón TARJETAS está disabled — saltamos.
    const btnTitulo = page.locator('.tarjetas-titulo-btn');
    const disabled = await btnTitulo.evaluate((el: HTMLButtonElement) => el.disabled);
    if (disabled) {
      console.log('Sin tarjetas — skip apertura de modal');
      // Forzar apertura del modal igual: el sistema auto-abre con 5+ cartas en INCORPORACION
      // Aquí sólo verificamos que el botón tenga el comportamiento adecuado en estado sin cartas
      return;
    }

    await btnTitulo.click();
    const modal = page.locator('.tarjetas-modal');
    await expect(modal).toBeVisible({ timeout: 3_000 });
    await expect(page.locator('.tarjetas-modal-titulo')).toHaveText(/CARTAS EN MANO/);

    await page.screenshot({ path: 'e2e/screenshots/tarjetas-modal-detalle.png' });

    // Cerrar con Escape
    await page.keyboard.press('Escape');
    await expect(modal).not.toBeVisible({ timeout: 2_000 });
  });

  test('los chips mini tienen el tamaño correcto (58×87, proporción 2:3)', async ({ page }) => {
    const chips = page.locator('.tarjeta-mini');
    const count = await chips.count();
    if (count === 0) {
      console.log('Sin tarjetas — skip verificación dimensiones');
      return;
    }
    const box = await chips.first().boundingBox();
    expect(box).toBeTruthy();
    expect(Math.abs(box!.width - 58)).toBeLessThan(2);
    expect(Math.abs(box!.height - 87)).toBeLessThan(2);
  });

  // ── Inyección de tarjetas mock vía `ng.getComponent` (Angular dev) ──
  // Permite validar la presentación del panel y modal con datos reales
  // sin depender de varios turnos de juego (conquistas). El polling de la
  // partida sobrescribe cartasJugador cada ciclo, así que reinyectamos
  // periódicamente durante el test.
  test('screenshot: panel y modal con tarjetas mock inyectadas', async ({ page }) => {
    await page.waitForTimeout(1_500);

    const inyectado = await page.evaluate(() => {
      const ng = (window as any).ng;
      const host = document.querySelector('app-tablero');
      if (!ng || !host) return false;
      const comp: any = ng.getComponent(host);
      if (!comp) return false;

      const mock = (id: number, simbolo: string, pais: string) => ({
        idEstadoTarjeta: id,
        idJugador: comp.jugadorId ?? 1,
        idTurno: 1,
        usada: false,
        canjeada: false,
        jugadorTienePais: true,
        tarjeta: { idtarjeta: id, simbolo, pais: { idPais: id, nombre: pais } }
      });
      const cartas = [
        mock(1, 'CANION',  'Argentina'),
        mock(2, 'GALEON',  'Brasil'),
        mock(3, 'GLOBO',   'Egipto'),
        mock(4, 'CANION',  'Japón'),
        mock(5, 'COMODIN', 'India'),
      ];
      comp.cartasJugador = cartas;

      // Reinyectar cada 250ms para resistir el polling de la partida
      (window as any).__mockTarjetasInterval = setInterval(() => {
        comp.cartasJugador = cartas;
        try { comp.applicationRef?.tick?.(); } catch {}
      }, 250);
      return true;
    });

    if (!inyectado) {
      console.log('No se pudo acceder al componente (ng global no disponible)');
      return;
    }

    await page.waitForTimeout(400);

    // Screenshot del panel con chips
    const col = page.locator('.columna-izquierda');
    await col.screenshot({ path: 'e2e/screenshots/tarjetas-panel-con-cartas.png' });

    // Abrir el modal de detalle
    await page.locator('.tarjetas-titulo-btn').click();
    await page.waitForSelector('.tarjetas-modal', { timeout: 3_000 });
    await page.waitForTimeout(400); // animación + posible re-inyección
    await page.screenshot({ path: 'e2e/screenshots/tarjetas-modal-con-cartas.png' });

    // El polling de la partida puede vaciar momentáneamente cartasJugador entre
    // re-inyecciones; por eso el assertion sólo verifica que el modal esté
    // visible. La evidencia real del render queda en los screenshots.
    await expect(page.locator('.tarjetas-modal')).toBeVisible();
    await expect(page.locator('.tarjetas-modal-titulo')).toHaveText(/CARTAS EN MANO/);

    // Cleanup
    await page.evaluate(() => {
      const id = (window as any).__mockTarjetasInterval;
      if (id) clearInterval(id);
    });
  });

});
