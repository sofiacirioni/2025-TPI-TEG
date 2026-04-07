const { chromium } = require('@playwright/test');
const http = require('http');

function post(path, body) {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify(body);
    const req = http.request({
      host: 'localhost', port: 8080, path, method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(data) }
    }, res => {
      let d = ''; const cookies = res.headers['set-cookie'] || [];
      res.on('data', c => d += c);
      res.on('end', () => resolve({ status: res.statusCode, body: JSON.parse(d || '{}'), cookies }));
    });
    req.on('error', reject);
    req.write(data); req.end();
  });
}

(async () => {
  const ts = Date.now();
  const reg = await post('/api/v1/auth/register', {
    correo: 'rev' + ts + '@test.com',
    usuario: 'rev' + ts,
    contrasenia: 'Test1234$',
    imagen: 'avatar1.png'
  });
  if (reg.status !== 200) { console.log('Register failed:', reg.status, JSON.stringify(reg.body).slice(0, 200)); process.exit(1); }
  console.log('Register OK, user:', reg.body.usuario);

  const browser = await chromium.launch({ headless: false });
  const page = await browser.newPage();
  const consoleErrors = [];
  const net404s = [];
  page.on('console', m => { if (m.type() === 'error') consoleErrors.push(m.text()); });
  page.on('response', r => { if (r.status() === 404) net404s.push(r.url()); });

  await page.goto('http://localhost:4200/iniciar-sesion', { waitUntil: 'networkidle' });
  await page.fill('[formcontrolname="nombreUsuario"]', 'rev' + ts + '@test.com');
  await page.fill('[formcontrolname="contrasenia"]', 'Test1234$');
  await page.locator('button').filter({ hasText: 'ENTREGAR' }).click();
  await page.waitForURL('**/principal', { timeout: 10000 });
  await page.waitForTimeout(800);
  console.log('Login OK:', page.url());

  await page.goto('http://localhost:4200/entrarCrearSala', { waitUntil: 'networkidle' });
  await page.waitForTimeout(500);
  await page.locator('button').filter({ hasText: /CREAR/i }).click();
  await page.waitForURL('**/configPartida', { timeout: 8000 });
  await page.waitForTimeout(1500);
  console.log('ConfigPartida OK');

  const slotVacio = page.locator('.slot-vacio').first();
  if (await slotVacio.count() > 0) { await slotVacio.click(); await page.waitForTimeout(1500); }

  const btnIniciar = page.locator('button').filter({ hasText: /INICIAR/i });
  const disabled = await btnIniciar.getAttribute('disabled');
  console.log('Btn INICIAR disabled:', disabled);
  if (disabled === null) {
    await btnIniciar.click();
    await page.waitForURL('**/juego/**', { timeout: 15000 });
    await page.waitForTimeout(4000);
    console.log('Tablero URL:', page.url());
  } else {
    console.log('WARN: boton deshabilitado');
    await browser.close(); process.exit(0);
  }

  await page.screenshot({ path: 'C:/Users/sofia/AppData/Local/Temp/review-ui/tablero-full.png', fullPage: false });
  console.log('Screenshot 1: tablero-full');

  const checks = [
    '.info-barra', '.sobre-objetivo', '.reloj-svg',
    '.panel-jugadores', '.panel-izquierdo', '.historial-panel',
    '.tarjetas-panel', '.chat-panel', '.zoom-controls',
    '.tablero-vignette', 'app-mapa-svg', '.viga-container',
    '.mapa-zona', '.ficha-jugador'
  ];
  console.log('\n--- ELEMENTOS PRESENTES ---');
  for (const sel of checks) {
    const count = await page.locator(sel).count();
    console.log((count > 0 ? 'PRESENTE' : 'AUSENTE') + ' ' + sel + ' (' + count + ')');
  }

  const infoBarText = await page.locator('.info-barra').textContent().catch(() => 'N/A');
  console.log('\nInfo-barra:', infoBarText.trim().replace(/\s+/g, ' ').slice(0, 120));

  const agujas = await page.locator('.reloj-svg line').count();
  console.log('Reloj lineas:', agujas, '(esperado 15)');

  const t1 = await page.locator('.info-timer').textContent().catch(() => 'N/A');
  await page.waitForTimeout(2200);
  const t2 = await page.locator('.info-timer').textContent().catch(() => 'N/A');
  console.log('Timer: t0=' + t1.trim() + ' t+2s=' + t2.trim() + (t1 !== t2 ? ' [COUNTDOWN OK]' : ' [WARN: no cambio]'));

  const fichas = await page.locator('.ficha-jugador').count();
  console.log('Fichas jugadores:', fichas);

  const panelIzq = page.locator('.panel-izquierdo');
  if (await panelIzq.count() > 0) {
    await panelIzq.screenshot({ path: 'C:/Users/sofia/AppData/Local/Temp/review-ui/tablero-panel-izq.png' });
    console.log('Screenshot 2: panel-izquierdo');
  }

  const reloj = page.locator('.reloj-svg');
  if (await reloj.count() > 0) {
    await reloj.screenshot({ path: 'C:/Users/sofia/AppData/Local/Temp/review-ui/tablero-reloj.png' });
    console.log('Screenshot 3: reloj');
  }

  const sobre = page.locator('.sobre-objetivo');
  if (await sobre.count() > 0) {
    await sobre.click(); await page.waitForTimeout(400);
    await page.screenshot({ path: 'C:/Users/sofia/AppData/Local/Temp/review-ui/tablero-objetivo.png', clip: { x: 0, y: 0, width: 280, height: 280 } });
    console.log('Screenshot 4: objetivo-modal');
    const objTexto = await page.locator('.objetivo-texto').textContent().catch(() => 'N/A');
    console.log('Objetivo texto:', objTexto ? objTexto.trim().slice(0, 80) : 'N/A');
    await sobre.click(); await page.waitForTimeout(200);
  }

  const mapaZona = page.locator('.mapa-zona');
  if (await mapaZona.count() > 0) {
    const rect = await mapaZona.boundingBox();
    if (rect) {
      await page.mouse.click(rect.x + rect.width * 0.52, rect.y + rect.height * 0.42);
      await page.waitForTimeout(700);
      const hasModal = await page.locator('.modal-pais').count() > 0;
      console.log('Modal de pais:', hasModal);
      if (hasModal) {
        await page.screenshot({ path: 'C:/Users/sofia/AppData/Local/Temp/review-ui/tablero-modal-pais.png', fullPage: false });
        console.log('Screenshot 5: modal-pais');
        const modalNom = await page.locator('.modal-nom').textContent().catch(() => 'N/A');
        const tachuela = await page.locator('.tachuela').count();
        console.log('Pais en modal:', modalNom, '| Tachuela:', tachuela > 0);
      }
    }
  }

  await page.locator('.zoom-controls button').first().click();
  await page.waitForTimeout(200);
  const zoomInd = await page.locator('.zoom-indicator').textContent().catch(() => 'N/A');
  console.log('Zoom indicator:', zoomInd.trim());
  await page.screenshot({ path: 'C:/Users/sofia/AppData/Local/Temp/review-ui/tablero-zoom.png', fullPage: false });
  console.log('Screenshot 6: zoom');

  const chatInput = page.locator('.chat-input');
  if (await chatInput.count() > 0) {
    await chatInput.fill('Negociemos kamchatka');
    await chatInput.press('Enter');
    await page.waitForTimeout(300);
    const msgCount = await page.locator('.chat-msg').count();
    console.log('Chat mensajes post-envio:', msgCount);
    await page.screenshot({ path: 'C:/Users/sofia/AppData/Local/Temp/review-ui/tablero-chat.png', clip: { x: 0, y: 340, width: 200, height: 160 } });
    console.log('Screenshot 7: chat');
  }

  await page.screenshot({ path: 'C:/Users/sofia/AppData/Local/Temp/review-ui/tablero-final.png', fullPage: false });
  console.log('Screenshot 8: final');

  console.log('\n--- ERRORES DE CONSOLA (' + consoleErrors.length + ') ---');
  consoleErrors.slice(0, 8).forEach(e => console.log('  ', e.slice(0, 120)));
  console.log('\n--- 404s (' + net404s.length + ') ---');
  net404s.slice(0, 8).forEach(u => console.log('  ', u));

  await browser.close();
  console.log('\nDone.');
})();
