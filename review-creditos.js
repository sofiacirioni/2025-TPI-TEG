// review-creditos.js — revisión focalizada en /creditos
const { chromium } = require('C:/Users/sofia/AppData/Local/npm-cache/_npx/e41f203b7505f1fb/node_modules/playwright');
const path = require('path');

const BASE = 'http://localhost:4200';
const SHOTS = 'C:/Users/sofia/Mis Archivos/proyectos-progra/TEG/2025-TPI-TEG/screenshots-review';

const results = { pass: [], fail: [] };
function pass(item) { results.pass.push(item); console.log('✓', item); }
function fail(item, expected, found, suggestion) {
  results.fail.push({ item, expected, found, suggestion });
  console.log('✗', item, '\n  esperado:', expected, '\n  encontrado:', found, '\n  sugerencia:', suggestion);
}

(async () => {
  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
  const page = await context.newPage();

  const consoleErrors = [];
  const notFound = [];
  const fontRequests = [];

  page.on('console', m => { if (m.type() === 'error') consoleErrors.push(m.text()); });
  page.on('response', r => {
    const url = r.url();
    if (r.status() === 404) notFound.push(url);
    if (url.includes('/assets/fonts/')) fontRequests.push(url);
  });

  await page.goto(`${BASE}/creditos`, { waitUntil: 'networkidle', timeout: 15000 });
  await page.waitForTimeout(2500);

  // ── Screenshot full-page ──────────────────────────────────────
  await page.screenshot({ path: `${SHOTS}/creditos-review.png`, fullPage: true });
  console.log('Screenshot: creditos-review.png\n');

  // ── 1. Imágenes 404 ───────────────────────────────────────────
  console.log('=== IMÁGENES ===');
  const img404 = notFound.filter(u => u.includes('/assets/'));
  if (img404.length === 0) {
    pass('Sin imágenes 404');
  } else {
    fail('Imágenes sin 404', '0 errores 404 de assets', `${img404.length}: ${img404.map(u=>u.split('/assets/')[1]).join(', ')}`, 'Verificar rutas ngSrc');
  }

  // Verificar que las 7 fotos cargan
  const imgCount = await page.evaluate(() =>
    document.querySelectorAll('.expediente-img').length
  );
  if (imgCount === 7) pass(`7 fotos de miembros presentes en DOM`);
  else fail('7 fotos de miembros', '7', String(imgCount), 'Verificar template HTML');

  // ── 2. Bootstrap sin override ─────────────────────────────────
  console.log('\n=== CLASES BOOTSTRAP SIN OVERRIDE ===');
  const bsRaw = await page.evaluate(() => {
    const bad = ['.btn-primary', '.btn-secondary', '.btn-success', '.card', '.badge', '.alert', '.bg-gradient'];
    const found = [];
    bad.forEach(sel => {
      const els = document.querySelectorAll(sel);
      if (els.length) found.push(`${sel}(${els.length})`);
    });
    return found;
  });
  if (bsRaw.length === 0) pass('Sin clases Bootstrap fuera de paleta');
  else fail('Sin clases Bootstrap sin override', 'ninguna', bsRaw.join(', '), 'Revisar template por clases residuales');

  // ── 3. Clases del design system ───────────────────────────────
  console.log('\n=== DESIGN SYSTEM ===');
  const dsClasses = await page.evaluate(() => {
    return {
      surfacePaper: document.querySelectorAll('.surface-paper--card').length,
      expedienteInfo: document.querySelectorAll('.expediente-info').length,
      expedienteFoto: document.querySelectorAll('.expediente-foto').length,
      habilidades: document.querySelectorAll('.expediente-habilidad').length,
      btnTeg: document.querySelectorAll('.btn-teg-dark, .btn-teg-light').length,
      tegH2: document.querySelectorAll('.teg-h2').length,
      legajos: document.querySelectorAll('.expediente-legajo').length,
      sceneGame: document.querySelectorAll('.scene-game').length,
      stamp: document.querySelectorAll('.creditos-stamp').length,
    };
  });
  if (dsClasses.surfacePaper >= 14) pass(`surface-paper--card: ${dsClasses.surfacePaper} elementos (foto + info × 7)`);
  else fail('surface-paper--card', '≥14', String(dsClasses.surfacePaper), 'Verificar clases en template');
  if (dsClasses.expedienteInfo === 7) pass('expediente-info: 7 tarjetas de info');
  else fail('expediente-info', '7', String(dsClasses.expedienteInfo), '');
  if (dsClasses.expedienteFoto === 7) pass('expediente-foto: 7 marcos de foto');
  else fail('expediente-foto', '7', String(dsClasses.expedienteFoto), '');
  if (dsClasses.habilidades === 21) pass(`expediente-habilidad: ${dsClasses.habilidades} (3 por miembro × 7)`);
  else pass(`expediente-habilidad: ${dsClasses.habilidades} habilidades`);
  if (dsClasses.legajos === 7) pass('expediente-legajo: 7 badges de legajo');
  else fail('expediente-legajo', '7', String(dsClasses.legajos), '');
  if (dsClasses.btnTeg >= 1) pass(`btn-teg-*: ${dsClasses.btnTeg} botón(es) con clase design system`);
  else fail('btn-teg-dark en botón Volver', '≥1', '0', 'Verificar botón volver en template');
  if (dsClasses.sceneGame === 1) pass('scene-game: fondo de escena aplicado');
  else fail('scene-game', '1', String(dsClasses.sceneGame), '');
  if (dsClasses.stamp === 1) pass('top-secret-stamp.svg presente');
  else fail('stamp', '1', '0', '');

  // ── 4. Tipografía ─────────────────────────────────────────────
  console.log('\n=== TIPOGRAFÍA ===');
  const fonts = await page.evaluate(() => {
    const alias = document.querySelector('.expediente-alias');
    const valor = document.querySelector('.expediente-valor');
    return {
      alias: alias ? window.getComputedStyle(alias).fontFamily : 'no encontrado',
      valor: valor ? window.getComputedStyle(valor).fontFamily : 'no encontrado',
    };
  });
  if (fonts.alias.includes('Special Elite')) pass(`Alias (h2): ${fonts.alias}`);
  else fail('Alias font', 'Special Elite', fonts.alias, '');
  if (fonts.valor.includes('Roboto Slab')) pass(`Valor text: ${fonts.valor}`);
  else fail('Valor font', 'Roboto Slab', fonts.valor, '');

  // ── 5. Errores de consola ─────────────────────────────────────
  console.log('\n=== CONSOLA ===');
  if (consoleErrors.length === 0) pass('Sin errores de consola');
  else {
    fail('Sin errores', '0 errores', `${consoleErrors.length}`, consoleErrors.slice(0,3).join(' | '));
  }

  // ── Resumen ───────────────────────────────────────────────────
  console.log('\n══════════════════════════════════════');
  console.log(`RESUMEN /creditos: ${results.pass.length} ✓  |  ${results.fail.length} ✗`);
  if (results.fail.length > 0) {
    console.log('\nFALLIDOS:');
    results.fail.forEach(f => console.log(`  ✗ ${f.item}: ${f.found}`));
  }

  await browser.close();
})();
