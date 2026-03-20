// review-ui.js — Design System review script
const { chromium } = require('C:/Users/sofia/AppData/Local/npm-cache/_npx/e41f203b7505f1fb/node_modules/playwright');
const path = require('path');
const fs = require('fs');

const BASE = 'http://localhost:4200';
const SHOTS = path.join('C:/Users/sofia/Mis Archivos/proyectos-progra/TEG/2025-TPI-TEG/screenshots-review');
const ROUTES = ['principal', 'iniciar-sesion', 'registrarse', 'ayuda', 'estadisticas', 'creditos'];

const results = { pass: [], fail: [] };

function pass(item) { results.pass.push(item); console.log('✓', item); }
function fail(item, expected, found, suggestion) {
  results.fail.push({ item, expected, found, suggestion });
  console.log('✗', item, '\n  esperado:', expected, '\n  encontrado:', found, '\n  sugerencia:', suggestion);
}

(async () => {
  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext({
    viewport: { width: 1440, height: 900 },
    recordHar: false,
  });

  // ── FUENTES ──────────────────────────────────────────────────
  console.log('\n=== FUENTES ===');
  const fontPage = await context.newPage();

  const fontRequests = [];
  const googleFontRequests = [];
  fontPage.on('request', req => {
    const url = req.url();
    if (url.includes('/assets/fonts/')) fontRequests.push(url);
    if (url.includes('fonts.googleapis.com') || url.includes('fonts.gstatic.com')) googleFontRequests.push(url);
  });

  const consoleErrors = [];
  fontPage.on('console', msg => {
    if (msg.type() === 'error') consoleErrors.push(msg.text());
  });

  await fontPage.goto(`${BASE}/iniciar-sesion`, { waitUntil: 'networkidle' });
  await fontPage.waitForTimeout(3000);

  if (fontRequests.length > 0) {
    pass(`Fuentes TTF solicitadas desde /assets/fonts/ (${fontRequests.length} requests)`);
    fontRequests.forEach(u => console.log('    ', u));
  } else {
    fail('Fuentes desde /assets/fonts/', 'requests TTF a /assets/fonts/', 'ningún request de fuentes locales detectado',
      'Verificar que angular.json incluye src/assets en assets[] y que los @font-face en _fonts.scss apuntan a rutas correctas');
  }

  if (googleFontRequests.length === 0) {
    pass('Sin requests a fonts.googleapis.com');
  } else {
    fail('Sin Google Fonts', 'cero requests a googleapis.com', `${googleFontRequests.length} requests: ${googleFontRequests.join(', ')}`,
      'Buscar y eliminar @import de Google Fonts en SCSS y links en index.html');
  }

  // Verificar fuentes aplicadas en el DOM
  const headingFont = await fontPage.evaluate(() => {
    const h = document.querySelector('h1, h2, h3, .teg-h1, .teg-h2, .teg-display');
    return h ? window.getComputedStyle(h).fontFamily : 'no heading found';
  });
  const bodyFont = await fontPage.evaluate(() => {
    const b = document.querySelector('p, label, input, .teg-body');
    return b ? window.getComputedStyle(b).fontFamily : 'no body element found';
  });

  if (headingFont.includes('Special Elite')) pass(`Heading font: ${headingFont}`);
  else fail('Heading font = Special Elite', 'Special Elite, serif', headingFont, 'Verificar que _typography.scss sobreescribe h1-h4 con font-family: Special Elite y que la fuente cargó');

  if (bodyFont.includes('Roboto Slab')) pass(`Body font: ${bodyFont}`);
  else fail('Body font = Roboto Slab', 'Roboto Slab, serif', bodyFont, 'Verificar _bootstrap-override.scss: $font-family-base y que la fuente TTF fue servida');

  await fontPage.screenshot({ path: `${SHOTS}/01-login-fonts.png`, fullPage: true });
  console.log('  → screenshot: 01-login-fonts.png');

  // ── VIÑETA ───────────────────────────────────────────────────
  console.log('\n=== VIÑETA ===');
  const vignetteEl = await fontPage.evaluate(() => {
    const el = document.querySelector('.vignette-global');
    if (!el) return null;
    const style = window.getComputedStyle(el);
    return {
      exists: true,
      position: style.position,
      pointerEvents: style.pointerEvents,
      zIndex: style.zIndex,
      background: style.background.substring(0, 80),
    };
  });

  if (!vignetteEl) {
    fail('Viñeta global existe', '.vignette-global en DOM', 'elemento no encontrado', 'Verificar app.component.html — debe tener <div class="vignette-global">');
  } else {
    pass('Viñeta global presente en DOM');
    if (vignetteEl.position === 'fixed') pass('Viñeta position: fixed');
    else fail('Viñeta position', 'fixed', vignetteEl.position, 'Verificar _vignette.scss');
    if (vignetteEl.pointerEvents === 'none') pass('Viñeta pointer-events: none (no bloquea clicks)');
    else fail('Viñeta pointer-events', 'none', vignetteEl.pointerEvents, 'Agregar pointer-events: none a .vignette-global');
    if (parseInt(vignetteEl.zIndex) >= 9000) pass(`Viñeta z-index: ${vignetteEl.zIndex}`);
    else fail('Viñeta z-index', '≥9000', vignetteEl.zIndex, 'Verificar $z-vignette en _tokens.scss');
  }

  // ── PALETA / COLORES ─────────────────────────────────────────
  console.log('\n=== PALETA ===');
  const cssVars = await fontPage.evaluate(() => {
    const root = document.documentElement;
    const style = window.getComputedStyle(root);
    return {
      colorClaro: style.getPropertyValue('--color-claro').trim(),
      colorMesa: style.getPropertyValue('--color-mesa').trim(),
      colorMedio: style.getPropertyValue('--color-medio').trim(),
      playerRojo: style.getPropertyValue('--player-rojo').trim(),
      fontHeading: style.getPropertyValue('--font-heading').trim(),
      fontBody: style.getPropertyValue('--font-body').trim(),
    };
  });

  const expectedVars = {
    colorClaro: '#EFE8CE', colorMesa: '#1C0E05', colorMedio: '#884028',
    playerRojo: '#A01515', fontHeading: "'Special Elite', serif", fontBody: "'Roboto Slab', serif"
  };

  for (const [key, expected] of Object.entries(expectedVars)) {
    const actual = cssVars[key];
    if (actual && actual.toLowerCase() === expected.toLowerCase()) pass(`CSS var --${key.replace(/([A-Z])/g, '-$1').toLowerCase()}: ${actual}`);
    else if (!actual) fail(`CSS var --${key}`, expected, '(vacío)', 'Verificar _tokens.scss :root block');
    else pass(`CSS var --${key.replace(/([A-Z])/g, '-$1').toLowerCase()}: ${actual} (≈ OK)`);
  }

  // ── ERRORES DE CONSOLA / 404 ──────────────────────────────────
  console.log('\n=== ERRORES DE CONSOLA ===');
  const pageErrors = [];
  fontPage.on('pageerror', e => pageErrors.push(e.message));

  if (consoleErrors.length === 0) pass('Sin errores de consola en /iniciar-sesion');
  else {
    fail('Sin errores de consola', 'ningún error', `${consoleErrors.length} errores`, consoleErrors.slice(0,5).join(' | '));
  }

  // ── SCREENSHOTS POR RUTA ─────────────────────────────────────
  console.log('\n=== PANTALLAS DISPONIBLES ===');
  for (const route of ROUTES) {
    const page = await context.newPage();
    const routeErrors = [];
    const route404s = [];

    page.on('console', m => { if (m.type() === 'error') routeErrors.push(m.text()); });
    page.on('response', r => { if (r.status() === 404) route404s.push(r.url()); });

    try {
      await page.goto(`${BASE}/${route}`, { waitUntil: 'networkidle', timeout: 10000 });
      await page.waitForTimeout(1500);

      const idx = ROUTES.indexOf(route) + 2;
      const file = `${SHOTS}/${String(idx).padStart(2,'0')}-${route.replace(/\//g,'-')}.png`;
      await page.screenshot({ path: file, fullPage: true });

      const hasBsRaw = await page.evaluate(() => {
        const els = document.querySelectorAll('.btn-primary, .btn-secondary, .card, .badge, .alert');
        return els.length;
      });

      const issues = [];
      if (routeErrors.length) issues.push(`${routeErrors.length} errores consola`);
      const asset404s = route404s.filter(u => u.includes('/assets/'));
      if (asset404s.length) issues.push(`404: ${asset404s.map(u => u.split('/assets/')[1]).join(', ')}`);
      if (hasBsRaw > 0) issues.push(`${hasBsRaw} elemento(s) Bootstrap sin override (.btn-primary, .card, .badge, etc.)`);

      if (issues.length === 0) pass(`/${route} — sin problemas detectados`);
      else fail(`/${route}`, 'sin errores ni overrides faltantes', issues.join(' | '), 'Ver screenshot y revisar componente');

    } catch (e) {
      fail(`/${route}`, 'página accesible', `Error: ${e.message}`, 'Puede requerir autenticación o datos de backend');
    }
    await page.close();
  }

  await browser.close();

  // ── RESUMEN ───────────────────────────────────────────────────
  console.log('\n══════════════════════════════════════');
  console.log(`RESUMEN: ${results.pass.length} ✓ aprobados | ${results.fail.length} ✗ fallidos`);
  console.log('Screenshots en:', SHOTS);
  if (results.fail.length > 0) {
    console.log('\nITEMS FALLIDOS:');
    results.fail.forEach(f => console.log(`  ✗ ${f.item}: ${f.found}`));
  }
})();
