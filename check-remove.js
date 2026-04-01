const { chromium } = require('C:/Users/sofia/AppData/Local/npm-cache/_npx/e41f203b7505f1fb/node_modules/playwright');

const SCREENSHOT_DIR = 'C:/Users/sofia/Mis Archivos/proyectos-progra/TEG/2025-TPI-TEG';

(async () => {
  const browser = await chromium.launch({ headless: false, slowMo: 200 });
  const context = await browser.newContext({ viewport: { width: 1280, height: 800 } });
  const page = await context.newPage();

  try {
    // Step 1: Login
    console.log('=== Step 1: Login ===');
    await page.goto('http://localhost:4200/iniciar-sesion', { waitUntil: 'networkidle', timeout: 15000 });
    await page.fill('input[type="text"]', 'test@test.com');
    await page.fill('input[type="password"]', 'Test123@');
    await page.click('button[type="submit"]');
    console.log('Submitted login form');

    // Wait for navigation away from login
    await page.waitForURL(url => !url.toString().includes('iniciar-sesion'), { timeout: 15000 });
    console.log('After login URL:', page.url().toString());
    await page.waitForTimeout(2000);

    // Step 2: Navigate to entrarCrearSala (clicking JUGAR if on principal)
    console.log('=== Step 2: Navigate to sala ===');
    const currentUrl = page.url().toString();
    if (!currentUrl.includes('entrarCrearSala')) {
      // Try clicking JUGAR button
      const jugarBtn = page.locator('button, a').filter({ hasText: /jugar/i }).first();
      if (await jugarBtn.isVisible({ timeout: 5000 }).catch(() => false)) {
        console.log('Clicking JUGAR button...');
        await jugarBtn.click();
        await page.waitForTimeout(1500);
      }

      // If still not there, go directly
      if (!page.url().toString().includes('entrarCrearSala')) {
        console.log('Navigating directly to /entrarCrearSala');
        await page.goto('http://localhost:4200/entrarCrearSala', { waitUntil: 'networkidle', timeout: 15000 });
      }
    }
    console.log('At URL:', page.url().toString());
    await page.waitForTimeout(1000);

    // Step 3: Select "Crear sala" option and fill name
    console.log('=== Step 3: Create sala ===');

    // Click on the "Establecer nueva sala" option (opcion A - crear)
    const crearOpcion = page.locator('.tele-opcion').filter({ hasText: /establecer|crear/i }).first();
    if (await crearOpcion.isVisible({ timeout: 5000 }).catch(() => false)) {
      console.log('Clicking "Crear sala" option...');
      await crearOpcion.click();
      await page.waitForTimeout(600);
    } else {
      console.log('WARNING: Could not find crear option by class, trying text...');
      const fallback = page.locator('text=/establecer|crear sala/i').first();
      await fallback.click().catch(() => console.log('Fallback click failed too'));
      await page.waitForTimeout(600);
    }

    // Fill the sala name input (campo-inp)
    const salaInput = page.locator('input.campo-inp').first();
    const inputVisible = await salaInput.isVisible({ timeout: 3000 }).catch(() => false);
    console.log('Sala name input visible:', inputVisible);

    if (inputVisible) {
      await salaInput.fill('TestRemove');
      console.log('Filled sala name: TestRemove');
    } else {
      // Try waiting for the animation/visibility transition
      await page.waitForTimeout(800);
      await salaInput.fill('TestRemove').catch(async () => {
        console.log('Still not visible, using force fill...');
        await page.evaluate(() => {
          const inputs = document.querySelectorAll('input.campo-inp');
          if (inputs[0]) (inputs[0]).value = 'TestRemove';
        });
      });
    }

    // Click confirm button
    const confirmarBtn = page.locator('button.btn-teg-primary').first();
    if (await confirmarBtn.isVisible({ timeout: 3000 }).catch(() => false)) {
      console.log('Clicking confirm button...');
      await confirmarBtn.click();
    } else {
      console.log('Trying Enter key...');
      await page.keyboard.press('Enter');
    }

    // Wait for navigation to configPartida
    await page.waitForURL(url => url.toString().includes('configPartida'), { timeout: 15000 });
    console.log('At configPartida:', page.url().toString());
    await page.waitForTimeout(1500);

    // Step 4: Screenshot 01 — initial config page
    console.log('=== Step 4: Screenshot 01 ===');
    await page.screenshot({ path: `${SCREENSHOT_DIR}/pw-config-remove-01.png`, fullPage: false });
    console.log('Screenshot 01 saved');

    // Step 5: Add 2 bots by clicking slot-vacio rows
    console.log('=== Step 5: Adding 2 bots ===');
    for (let i = 0; i < 2; i++) {
      console.log(`Adding bot ${i + 1}...`);

      // Wait for a slot-vacio to be available
      await page.waitForSelector('tr.slot-vacio', { timeout: 5000 }).catch(() => {
        console.log(`No slot-vacio found for bot ${i + 1}`);
      });

      const slotVacio = page.locator('tr.slot-vacio').first();
      const slotVisible = await slotVacio.isVisible({ timeout: 3000 }).catch(() => false);
      console.log(`slot-vacio visible: ${slotVisible}`);

      if (slotVisible) {
        await slotVacio.click();
        console.log(`Clicked slot-vacio for bot ${i + 1}`);
      } else {
        console.log(`Cannot add bot ${i + 1} - no slot-vacio found`);
        break;
      }

      // Wait for the list to update
      await page.waitForTimeout(2500);
    }

    // Step 6: Screenshot 02 — with bots added
    console.log('=== Step 6: Screenshot 02 ===');
    await page.screenshot({ path: `${SCREENSHOT_DIR}/pw-config-remove-02.png`, fullPage: false });
    console.log('Screenshot 02 saved');

    // Count fila-jugador rows before removal
    const jugadorRowsBefore = page.locator('tr.fila-jugador');
    const countBefore = await jugadorRowsBefore.count();
    console.log(`Fila-jugador count before removal: ${countBefore}`);

    // Step 7: Click btn-eliminar on first bot row
    // The btn-eliminar is conditionally shown for bots and non-host players
    // Host is row 0, bots are rows 1+
    console.log('=== Step 7: Remove bot ===');

    // Find all btn-eliminar buttons
    const allBtnEliminar = page.locator('.btn-eliminar');
    const btnCount = await allBtnEliminar.count();
    console.log(`Found ${btnCount} .btn-eliminar buttons`);

    if (btnCount > 0) {
      const firstBtn = allBtnEliminar.first();

      // Check if the button is in a bot row (not host)
      // The button is visible since it's conditionally rendered (not hidden by CSS)
      const isVisible = await firstBtn.isVisible({ timeout: 3000 }).catch(() => false);
      console.log(`First btn-eliminar visible: ${isVisible}`);

      if (isVisible) {
        // Hover the parent row for good measure
        const parentRow = page.locator('tr.fila-jugador').filter({ has: firstBtn }).first();
        await parentRow.hover().catch(() => console.log('Hover failed, continuing...'));
        await page.waitForTimeout(500);

        console.log('Clicking first btn-eliminar...');
        await firstBtn.click();
        console.log('Clicked btn-eliminar');
      } else {
        console.log('btn-eliminar not visible, trying force click...');
        await firstBtn.click({ force: true });
      }
    } else {
      console.log('ERROR: No btn-eliminar found. Are bots present?');
      // List all rows for debugging
      const rows = await page.locator('tr').all();
      console.log(`Total tr elements: ${rows.length}`);
    }

    // Step 8: Wait for list to update
    console.log('=== Step 8: Wait for update ===');
    await page.waitForTimeout(2000);

    const countAfter = await page.locator('tr.fila-jugador').count();
    console.log(`Fila-jugador count after removal: ${countAfter}`);

    // Step 9: Screenshot 03 — after removal
    console.log('=== Step 9: Screenshot 03 ===');
    await page.screenshot({ path: `${SCREENSHOT_DIR}/pw-config-remove-03.png`, fullPage: false });
    console.log('Screenshot 03 saved');

    // Final verdict
    console.log('\n=== RESULT ===');
    if (countAfter < countBefore) {
      console.log(`SUCCESS: Bot removed! Rows: ${countBefore} → ${countAfter}`);
    } else {
      console.log(`WARNING: Row count did not decrease. Before: ${countBefore}, After: ${countAfter}`);
    }

  } catch (err) {
    console.error('Error:', err.message);
    await page.screenshot({ path: `${SCREENSHOT_DIR}/pw-config-remove-error.png`, fullPage: false }).catch(() => {});
    console.log('Error screenshot saved');
    throw err;
  } finally {
    await page.waitForTimeout(2000);
    await browser.close();
  }
})();
