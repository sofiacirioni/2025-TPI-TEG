# Revisión visual UI — TEG Design System

Usá Playwright para hacer una revisión visual completa de la interfaz 
actual contra los criterios del design system TEG.

## Checklist a verificar

### Fuentes
- [ ] Abrí DevTools Network, filtrá por "font"
- [ ] Verificar que se cargan archivos TTF desde /assets/fonts/
- [ ] Verificar que NO hay requests a fonts.googleapis.com
- [ ] Capturar screenshot de un título H1 o display — debe verse 
      Special Elite (tipografía de máquina de escribir)
- [ ] Capturar screenshot de párrafo de cuerpo — debe verse Roboto Slab 
      (serif con remates)

### Cursor
- [ ] Verificar que el cursor custom aparece (no el cursor de flecha 
      nativo del sistema)
- [ ] Hover sobre un botón — debe cambiar a la mano señalando

### Viñeta
- [ ] Capturar screenshot full-page
- [ ] Los cuatro bordes deben tener oscurecimiento gradual visible
- [ ] El centro debe estar más iluminado que los bordes

### Atmósfera general
- [ ] La pantalla visible se percibe como "documento de época" o 
      "interfaz militar histórica"
- [ ] No hay elementos que rompan la coherencia visual (colores 
      fuera de paleta, fuentes modernas, iconos muy contemporáneos)

### Componentes existentes
- [ ] Navegar a cada ruta disponible en app.routes.ts
- [ ] Capturar screenshot de cada pantalla
- [ ] Identificar cualquier componente que use clases Bootstrap 
      sin override (btn btn-primary, card, badge, etc.) y que 
      visualmente no coincida con el design system

### Errores de consola
- [ ] Verificar que no hay errores 404 de assets (fuentes, SVGs, imágenes)
- [ ] Verificar que no hay errores de Angular en consola

## Formato del reporte

Para cada ítem fallido, reportar:
- Qué se esperaba
- Qué se encontró
- Screenshot o selector CSS del elemento problemático
- Sugerencia de corrección

Para cada ítem aprobado, una línea: ✓ [nombre del ítem]