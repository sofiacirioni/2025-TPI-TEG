/**
 * demo-pantallas.spec.ts
 * ──────────────────────────────────────────────────────────────────────
 * Recorrido de demostración por todas las pantallas del juego, pensado
 * para grabar el video del proyecto.
 *
 * A diferencia del resto de los specs, este no verifica nada: sólo navega
 * en un orden legible, con pausas para que se lean los textos y se vean
 * las animaciones. Playwright graba el video por su cuenta.
 *
 * Uso:
 *   npx playwright test e2e/demo-pantallas.spec.ts --project=chromium
 *
 * El video queda en test-results/<nombre-del-test>/video.webm
 *
 * Para grabar con el escritorio (Win+Alt+R) en vez del video de
 * Playwright, correrlo igual: el navegador se abre visible.
 * ──────────────────────────────────────────────────────────────────────
 */

import { test, expect, Page } from '@playwright/test';
import { readFileSync, existsSync } from 'fs';
import path from 'path';

// El setup deja acá la URL de una partida ya iniciada con bots.
const URL_FILE = path.join(__dirname, '.auth/tablero-url.txt');

/** Pausa expresiva: le da tiempo al espectador a leer la pantalla. */
async function mirar(page: Page, segundos = 3) {
  await page.waitForTimeout(segundos * 1000);
}

/**
 * Cierra el modal de país si quedó abierto. El selector es `.modal-close-x`:
 * `.btn-modal-cerrar`, que usa flujo-completo.spec.ts, no existe en el markup
 * y por eso ahí los cierres fallan en silencio.
 */
async function cerrarModal(page: Page) {
  await page.locator('.modal-close-x').click({ timeout: 1_000 }).catch(() => {});
  await page.waitForTimeout(300);
}

/** Avanza a la fase siguiente del turno. */
async function avanzarFase(page: Page) {
  await page.locator('.btn-avanzar').click({ timeout: 5_000 }).catch(() => {});
  await page.waitForTimeout(1_200);
}

/**
 * Coloca un refuerzo en el país número `i` del mapa, si es propio y estamos en
 * fase de incorporación. Devuelve true si llegó a colocar.
 */
async function colocarEnPais(page: Page, i: number): Promise<boolean> {
  // El modal abierto tapa el mapa y se come el click siguiente.
  await cerrarModal(page);
  await page.locator('.mapa-svg path').nth(i).click({ timeout: 5_000 }).catch(() => {});
  const modal = page.locator('.modal-pais');
  if (!(await modal.isVisible({ timeout: 1_500 }).catch(() => false))) return false;

  const input = modal.locator('input[type="number"]').first();
  const boton = modal.locator('button:has-text("COLOCAR")');
  if (!(await input.isVisible({ timeout: 500 }).catch(() => false)) ||
      !(await boton.isVisible({ timeout: 500 }).catch(() => false))) {
    await cerrarModal(page);
    return false;
  }
  await input.fill('1');
  await boton.click();
  await page.waitForTimeout(1_200);
  await cerrarModal(page);
  return true;
}

/**
 * Lanza un ataque desde el país número `i`, eligiendo el primer enemigo
 * limítrofe y tirando los dados. Devuelve true si llegó a combatir.
 */
async function atacarDesde(page: Page, i: number): Promise<boolean> {
  await cerrarModal(page);
  await page.locator('.mapa-svg path').nth(i).click({ timeout: 5_000 }).catch(() => {});
  const modal = page.locator('.modal-pais');
  if (!(await modal.isVisible({ timeout: 1_500 }).catch(() => false))) return false;

  const boton = modal.locator('button:has-text("ATACAR")');
  const select = modal.locator('select').first();
  if (!(await boton.isVisible({ timeout: 500 }).catch(() => false)) ||
      !(await select.isVisible({ timeout: 500 }).catch(() => false))) {
    await cerrarModal(page);
    return false;
  }

  // La primera opción suele ser el placeholder; se toma la siguiente.
  const opciones = await select.locator('option').count();
  if (opciones < 2) {
    await cerrarModal(page);
    return false;
  }
  await select.selectOption({ index: 1 });
  await boton.click();

  // Panel de dados: se elige la cantidad y el combate se resuelve solo.
  const dados = page.locator('.ted-dice-btn').first();
  if (await dados.isVisible({ timeout: 5_000 }).catch(() => false)) {
    await dados.click();
  }
  await page.waitForTimeout(2_000);
  await page.locator('.btn-entendido').click({ timeout: 3_000 }).catch(() => {});
  await cerrarModal(page);
  return true;
}

// Va a nivel de archivo y no dentro del describe: activar el video obliga a
// Playwright a levantar un worker nuevo, y eso sólo puede declararse acá.
test.use({
  video: { mode: 'on', size: { width: 1920, height: 1080 } },
  viewport: { width: 1920, height: 1080 },
});

test.describe('Recorrido de demostración', () => {
  test('recorrido completo por las pantallas', async ({ page, context }) => {
    // El recorrido está lleno de pausas expresivas: no entra en el timeout
    // por defecto de 30 s.
    test.setTimeout(6 * 60 * 1000);

    // La sesión guardada por el setup saltearía el login, que es justo lo
    // que se quiere mostrar. Se arranca limpio.
    await context.clearCookies();

    // ── 1. Intro ──────────────────────────────────────────────────────
    await page.goto('/');
    await mirar(page, 6); // la máquina de escribir tarda en terminar

    // ── 2. Login ──────────────────────────────────────────────────────
    await page.goto('/iniciar-sesion');
    await mirar(page, 2);
    // Se escribe carácter por carácter: en video se ve mucho mejor que
    // un fill() instantáneo.
    await page.locator('#nombreUsuario').pressSequentially('test@test.com', { delay: 70 });
    await page.locator('#contrasenia').pressSequentially('Test123@', { delay: 70 });
    await mirar(page, 1);
    await page.getByRole('button', { name: /entregar/i }).click();
    await page.waitForURL('**/principal', { timeout: 20_000 });

    // ── 3. Menú principal ─────────────────────────────────────────────
    await mirar(page, 4);

    // ── 4. Reglamento — carpeta de archivo con pestañas ───────────────
    await page.goto('/ayuda');
    await page.waitForSelector('.carpeta-pestanias');
    await mirar(page, 3);
    // Se pasa por cada pestaña para que se vea el pliegue de la carpeta.
    const pestanias = page.locator('.pestania');
    for (let i = 1; i < (await pestanias.count()); i++) {
      await pestanias.nth(i).click();
      await mirar(page, 2);
    }

    // ── 5. Perfil — libreta de enrolamiento ───────────────────────────
    await page.goto('/perfilUsuario');
    await page.waitForSelector('.libreta', { timeout: 20_000 });
    await mirar(page, 4);
    // La galería de fotografías del legajo.
    await page.locator('.legajo-cambiar-foto').click();
    await page.waitForSelector('.galeria');
    await mirar(page, 3);
    await page.locator('.tramite-cerrar').click();
    await mirar(page, 1);

    // ── 6. Tablero ────────────────────────────────────────────────────
    // El setup deja una partida iniciada; si no corrió, se salta esta parte.
    if (!existsSync(URL_FILE)) {
      console.warn('Sin partida preparada: correr primero el proyecto "setup".');
      return;
    }
    const tableroUrl = readFileSync(URL_FILE, 'utf-8').trim();
    await page.goto(tableroUrl);
    await page.waitForSelector('app-mapa-svg svg', { timeout: 30_000 });
    await mirar(page, 4);

    // El objetivo secreto, en el sobre de arriba a la izquierda: se abre
    // como una orden lacrada y se cierra al hacerle click encima.
    await page.locator('.sobre-objetivo').click();
    await page.waitForSelector('.objetivo-modal');
    await mirar(page, 5);
    await page.locator('.objetivo-modal').click();
    await mirar(page, 1);

    // ── 7. Un turno jugado ────────────────────────────────────────────
    const totalPaises = await page.locator('.mapa-svg path').count();

    // Incorporación: se reparten refuerzos en un par de países propios.
    let colocados = 0;
    for (let i = 0; i < totalPaises && colocados < 2; i++) {
      if (await colocarEnPais(page, i)) {
        colocados++;
        await mirar(page, 2);
      }
    }

    // Ataque: se avanza de fase y se lanza un combate con dados.
    await avanzarFase(page);
    await mirar(page, 2);

    for (let i = 0; i < totalPaises; i++) {
      if (await atacarDesde(page, i)) {
        // Los dados y el resultado del combate son lo más vistoso del juego.
        await mirar(page, 6);
        break;
      }
    }
    // El historial de la derecha registra lo que acaba de pasar.
    await mirar(page, 3);

    // Tratados: el wizard de pactos, aunque no se llegue a firmar.
    const btnPacto = page.locator('.btn-pacto, [class*="pacto"]:is(button)').first();
    if (await btnPacto.isVisible({ timeout: 1_000 }).catch(() => false)) {
      await btnPacto.click();
      await page.waitForSelector('.pactos-overlay', { timeout: 5_000 }).catch(() => {});
      await mirar(page, 6);
      await page.keyboard.press('Escape');
      await mirar(page, 1);
    }

    // Reagrupamiento y cierre del turno: los bots juegan a continuación.
    await avanzarFase(page);
    await mirar(page, 3);
    await avanzarFase(page);
    // Cada bot tarda unos 15 s; se muestran dos turnos de máquina.
    await mirar(page, 30);

    // ── 8. Fin de partida — el diario de guerra ───────────────────────
    // El diario se dispara con el evento WebSocket FIN_PARTIDA y vive sólo
    // en memoria del componente: no hay URL que lo abra ni forma de forzarlo
    // sin terminar la partida, que puede llevar media hora contra los bots.
    //
    // Para la demo se arma el desenlace con los jugadores REALES de la
    // partida en curso —nombres, colores y avatares de verdad— y se declara
    // vencedor al humano. Se inyecta con la API de depuración de Angular,
    // que existe sólo en modo desarrollo; por eso esto es un recorrido de
    // demostración y no un test.
    const diarioMostrado = await page.evaluate(() => {
      type Jugador = {
        idJugador: number; nombre: string; color: string; url?: string;
        ejercito?: number; objetivo?: { descripcion?: string };
      };
      type Tablero = {
        partida?: { jugadores?: Jugador[] };
        jugadorUsuario?: Jugador;
        datosFinPartida?: unknown;
        mostrarFinPartida?: boolean;
        partidaFinalizada?: boolean;
      };
      const ng = (window as unknown as {
        ng?: { getComponent(el: Element): Tablero; applyChanges(c: unknown): void };
      }).ng;
      const host = document.querySelector('app-tablero');
      if (!ng || !host) return false;

      const tablero = ng.getComponent(host);
      const jugadores = tablero.partida?.jugadores ?? [];
      if (!jugadores.length) return false;

      const ganador = tablero.jugadorUsuario ?? jugadores[0];
      const clasificacion = jugadores.map((j, i) => ({
        id: j.idJugador,
        nombre: j.nombre,
        color: j.color,
        avatarUrl: j.url,
        // El vencedor llega a los 30 países del objetivo común; el resto
        // queda repartido de mayor a menor.
        cantidadPaises: j.idJugador === ganador.idJugador ? 30 : Math.max(1, 11 - i * 3),
        cantidadEjercitos: j.ejercito ?? 0,
        eliminado: j.idJugador !== ganador.idJugador && i >= 2,
      }));

      tablero.datosFinPartida = {
        ganador,
        objetivoCumplido: {
          descripcion: ganador.objetivo?.descripcion ?? 'Ocupar 30 países',
          items: [],
          completado: true,
          objetivoConvertido: false,
        },
        clasificacion,
        momentoFin: new Date().toISOString(),
      };
      // partidaFinalizada evita que un evento del backend vuelva a dispararlo.
      tablero.partidaFinalizada = true;
      tablero.mostrarFinPartida = true;
      ng.applyChanges(tablero);
      return true;
    });

    if (diarioMostrado) {
      await expect(page.locator('app-fin-partida-overlay')).toBeVisible({ timeout: 10_000 });
      // Las medallas caen sobre el diario con una animación de GSAP.
      await mirar(page, 12);
    } else {
      console.warn('No se pudo inyectar el fin de partida: ¿el frontend corre en modo producción?');
    }

    // ── 9. Parte de campaña — la pizarra ──────────────────────────────
    // Funciona con cualquier partida: si no terminó, se rotula como
    // "campaña sin resolver". El id no está en la URL del tablero (que
    // lleva el uuid de la sala), así que se lee del componente Angular.
    const idPartida = await page.evaluate(() => {
      const ng = (window as unknown as { ng?: { getComponent(el: Element): { partida?: { idPartida?: number } } } }).ng;
      const host = document.querySelector('app-tablero');
      return ng && host ? ng.getComponent(host)?.partida?.idPartida : undefined;
    });
    test.skip(!idPartida, 'No se pudo resolver el id de la partida activa.');
    await page.goto(`/estadisticas/${idPartida}`);
    // La pizarra puede tardar: arma el resumen de todos los comandantes.
    await expect(page.locator('.pizarra-titulo')).toBeVisible({ timeout: 20_000 });
    await mirar(page, 6);

    // ── 10. Cierre: de vuelta al cuartel ──────────────────────────────
    await page.locator('.btn-volver').click();
    await page.waitForURL('**/principal');
    await mirar(page, 3);
  });
});
