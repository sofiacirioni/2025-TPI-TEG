# 📘 Comandos útiles de Angular CLI

Esta guía rápida reúne los comandos más importantes que vas a usar durante el desarrollo del proyecto. Todos deben ejecutarse desde la raíz del proyecto Angular, utilizando la terminal.

---

## 🔧 Comandos de generación

| Tarea                                 | Comando Angular CLI                                   |
|--------------------------------------|--------------------------------------------------------|
| Crear un componente                  | `ng generate component nombre-componente`             |
| Crear un servicio                    | `ng generate service nombre-servicio`                 |
| Crear una interfaz                   | `ng generate interface nombre-interfaz`               |
| Crear una clase                      | `ng generate class nombre-clase`                      |
| Crear una directiva                  | `ng generate directive nombre-directiva`              |
| Crear un pipe                        | `ng generate pipe nombre-pipe`                        |
| Crear una enumeración (enum)        | `ng generate enum nombre-enum`                        |


> ✅ Tip: También podés usar los alias `ng g c`, `ng g s`, `ng g i`, etc.

---

## ▶️ Comandos para ejecutar y compilar

| Tarea                                  | Comando                         |
|---------------------------------------|----------------------------------|
| Ejecutar la aplicación                | `ng serve`                      |
| Ejecutar con puerto específico        | `ng serve --port=4300`          |
| Ejecutar para producción              | `ng build --configuration=production` |
| Ejecutar con modo watch (auto-reload) | `ng serve --watch`              |

---

## 🌍 Comandos útiles extra

| Tarea                                  | Comando                             |
|---------------------------------------|--------------------------------------|
| Mostrar ayuda de un comando           | `ng help generate`                  |
| Ver versión de Angular instalada      | `ng version`                        |
| Instalar dependencias (npm)           | `npm install`                       |
| Instalar una librería                 | `npm install nombre-paquete`        |

---

## 🧠 Consejos

- Siempre que crees componentes, servicios, pipes o interfaces, organizalos dentro de carpetas como `/components`, `/services`, `/models`, etc.
- Usá nombres claros y descriptivos.
- No tengas miedo de usar el CLI. Evitá crear archivos manualmente, salvo que sepas exactamente qué hacés.

---
## 🌍 Environments en Angular

Angular permite definir distintos entornos (desarrollo, producción, etc.) usando archivos de configuración ubicados en `src/environments`.

### Ejemplo de `environment.ts` (desarrollo):

```ts
// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:3000/api',
  mapaUrl: 'https://api-teg.dev/mapa'
};
````

### Ejemplo de `environment.prod.ts` (producción):

```ts
// src/environments/environment.prod.ts
export const environment = {
  production: true,
  apiUrl: 'https://api.juegoteg.com/api',
  mapaUrl: 'https://api.juegoteg.com/mapa'
};
```

### ¿Cómo usar el `environment`?

Lo podés importar en cualquier servicio, componente o clase para acceder a las URLs o configuraciones definidas según el entorno:

```ts
// ejemplo en un servicio
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MapaService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  obtenerPaises() {
    return this.http.get(`${this.apiUrl}/paises`);
  }
}
```

---

## 📦 ¿Qué se compila según el entorno?

Al ejecutar el proyecto con:

```bash
ng serve
```

Se toma por defecto `environment.ts`.

Pero si hacés build para producción:

```bash
ng build --configuration=production
```

Angular reemplaza automáticamente `environment.ts` por `environment.prod.ts`.

---


