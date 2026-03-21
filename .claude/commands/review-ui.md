# Revisión visual UI — TEG Design System

Ejecuta una revisión visual completa de la interfaz usando Playwright. El frontend debe estar corriendo en http://localhost:4200. Si no está corriendo, levantalo con `cd Frontend && npm start` antes de empezar.

## Instrucciones

Usá el Bash tool para correr comandos de Playwright. Capturá screenshots con `page.screenshot()` y guardálos en `/tmp/review-ui/`. Analizá cada screenshot visualmente.

### 1. Setup

```bash
mkdir -p /tmp/review-ui
```

Iniciá un script Playwright inline con Node.js:

```bash
cd "Frontend" && node -e "
const { chromium } = require('@playwright/test');
(async () => {
  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext();
  const page = await context.newPage();
  const errors = [];
  const requests404 = [];

  page.on('console', msg => { if (msg.type() === 'error') errors.push(msg.text()); });
  page.on('response', res => { if (res.status() === 404) requests404.push(res.url()); });

  const routes = [
    'principal', 'iniciar-sesion', 'registrarse', 'perfilUsuario',
    'entrarCrearSala', 'configPartida', 'creditos', 'estadisticas', 'ayuda'
  ];

  for (const route of routes) {
    await page.goto('http://localhost:4200/' + route, { waitUntil: 'networkidle' });
    await page.waitForTimeout(1000);
    await page.screenshot({ path: '/tmp/review-ui/' + route + '.png', fullPage: true });
    console.log('Capturado: ' + route);
  }

  console.log('--- ERRORES DE CONSOLA ---');
  errors.forEach(e => console.log(e));
  console.log('--- REQUESTS 404 ---');
  requests404.forEach(u => console.log(u));

  await browser.close();
})();
"
```

### 2. Checklist a verificar

Para cada screenshot capturado, revisá visualmente:

**Fuentes**
- El texto de títulos/headings usa Special Elite (tipografía de máquina de escribir)
- El texto de cuerpo usa Roboto Slab (serif con remates)
- No hay fuentes sans-serif modernas visibles (Arial, Helvetica, etc.)

**Viñeta (vignette)**
- Los cuatro bordes de la pantalla tienen oscurecimiento gradual
- El centro está más iluminado que los bordes

**Fondo**
- Pantalla `principal`: fondo `initial-scene-war-room.webp` (sala de guerra)
- Resto de pantallas: fondo `game-scene-table.webp` (mesa de juego)
- El fondo no se ve ampliado, pixelado ni cortado extrañamente
- El fondo es consistente en todas las pantallas (mismo nivel de zoom)

**Coherencia visual**
- Paleta de colores acorde al design system (tonos marrones, rojos oscuros, crema)
- No hay botones Bootstrap sin override (btn-primary azul, card gris, etc.)
- No hay iconos muy contemporáneos que rompan la estética

**Errores técnicos**
- Sin errores 404 en assets (fuentes, SVGs, imágenes)
- Sin errores de Angular en consola

### 3. Formato del reporte

Al finalizar, producí un reporte con este formato:

Para cada ítem aprobado:
`✓ [nombre del ítem]`

Para cada ítem fallido:
```
✗ [nombre del ítem]
  Esperado: ...
  Encontrado: ...
  Pantalla/selector: ...
  Sugerencia: ...
```