# 🎮 TEG - Monorepo

Aplicación web para jugar **TEG** (Táctica y Estrategia de Guerra), un juego de estrategia y conquista.

Este proyecto es un monorepo que contiene:
- **Frontend**: Angular 20
- **Backend**: Java 17 + Spring Boot 3.5.0

---

## 📋 Requisitos Previos

### Para el Backend:
- **Java JDK 17** o superior
- **Maven 3.6+** (o usar el wrapper incluido: `mvnw`)

### Para el Frontend:
- **Node.js 18+** y **npm** (o **yarn**)

---

## 🚀 Instalación y Levantado

### 1️⃣ Backend (Java/Spring Boot)

#### Opción A: Usando Maven Wrapper (Recomendado)

```bash
# Navegar a la carpeta del backend
cd Backend

# Instalar dependencias y compilar (primera vez)
./mvnw clean install

# En Windows:
mvnw.cmd clean install

# Levantar el servidor
./mvnw spring-boot:run

# En Windows:
mvnw.cmd spring-boot:run
```

#### Opción B: Usando Maven instalado

```bash
cd Backend

# Instalar dependencias y compilar
mvn clean install

# Levantar el servidor
mvn spring-boot:run
```

#### Verificar que el Backend está corriendo:
- El servidor se levantará en: **http://localhost:8080**
- Swagger UI (documentación API): **http://localhost:8080/swagger-ui.html**
- H2 Console (base de datos): **http://localhost:8080/h2-console**

**Credenciales H2 Console:**
- JDBC URL: `jdbc:h2:file:./data/tegdb`
- Username: `sa`
- Password: (vacío)

---

### 2️⃣ Frontend (Angular)

```bash
# Navegar a la carpeta del frontend
cd Frontend

# Instalar dependencias (primera vez)
npm install

# Levantar el servidor de desarrollo
npm start

# O alternativamente:
ng serve
```

#### Verificar que el Frontend está corriendo:
- El servidor se levantará en: **http://localhost:4200**

---

## 🔧 Configuración

### Backend

La configuración del backend se encuentra en:
```
Backend/src/main/resources/application.properties
```

**Configuración actual:**
- Puerto: `8080`
- Base de datos: H2 (archivo persistente en `Backend/data/tegdb.mv.db`)
- CORS: Habilitado para `http://localhost:4200`

### Frontend

La configuración del frontend se encuentra en:
```
Frontend/src/environments/environment.ts
```

**Configuración actual:**
- API URL: `http://localhost:8080/api/v1`
- WebSocket URL: `http://localhost:8080/ws`

Si necesitas cambiar el puerto del backend, actualiza:
1. `Backend/src/main/resources/application.properties` → `server.port`
2. `Frontend/src/environments/environment.ts` → `apiUrl` y `wsUrl`

---

## 📁 Estructura del Proyecto

```
2025-TPI-TEG/
├── Backend/              # Spring Boot Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/     # Código fuente Java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/         # Tests
│   ├── data/             # Base de datos H2 (generada automáticamente)
│   ├── pom.xml           # Dependencias Maven
│   └── mvnw              # Maven Wrapper
│
├── Frontend/             # Angular Frontend
│   ├── src/
│   │   ├── app/          # Código fuente Angular
│   │   ├── assets/       # Recursos estáticos
│   │   └── environments/ # Configuración de entornos
│   ├── package.json      # Dependencias npm
│   └── angular.json      # Configuración Angular
│
└── .gitignore           # Archivos ignorados por Git (unificado)
```

---

## 🛠️ Comandos Útiles

### Backend

```bash
# Compilar sin ejecutar
./mvnw clean package

# Ejecutar tests
./mvnw test

# Limpiar proyecto
./mvnw clean
```

### Frontend

```bash
# Compilar para producción
npm run build

# Ejecutar tests
npm test

# Verificar código
ng lint
```

---

## 🐛 Solución de Problemas

### Backend no inicia
1. Verifica que Java 17+ esté instalado: `java -version`
2. Verifica que el puerto 8080 no esté en uso
3. Revisa los logs en la consola para errores específicos

### Frontend no se conecta al Backend
1. Verifica que el backend esté corriendo en `http://localhost:8080`
2. Revisa la consola del navegador (F12) para errores CORS
3. Verifica que `Frontend/src/environments/environment.ts` tenga la URL correcta

### Errores de dependencias
```bash
# Backend: Limpiar y reinstalar
cd Backend
./mvnw clean install

# Frontend: Eliminar node_modules y reinstalar
cd Frontend
rm -rf node_modules package-lock.json
npm install
```

---

## 📚 Documentación Adicional

- **API Documentation**: http://localhost:8080/swagger-ui.html (cuando el backend esté corriendo)
- **JavaDoc**: `Backend/docs/java_doc/`
- **Documentación de la App**: `Backend/docs/app_doc/`

---

## 👥 Desarrollo

### Flujo de trabajo recomendado:

1. **Iniciar Backend primero** (puerto 8080)
2. **Luego iniciar Frontend** (puerto 4200)
3. Abrir el navegador en `http://localhost:4200`

### Hot Reload:
- **Backend**: Spring Boot DevTools está configurado (reinicio automático)
- **Frontend**: Angular CLI tiene hot reload por defecto

---

## 📝 Notas

- La base de datos H2 es persistente (archivo en `Backend/data/`)
- Los datos se mantienen entre reinicios del servidor
- Para resetear la base de datos, elimina los archivos `.mv.db` y `.trace.db` en `Backend/data/`

---

## ✅ Checklist de Verificación

Antes de empezar a desarrollar, verifica:

- [ ] Java 17+ instalado y en PATH
- [ ] Node.js 18+ instalado
- [ ] Backend se levanta correctamente en puerto 8080
- [ ] Frontend se levanta correctamente en puerto 4200
- [ ] Frontend puede comunicarse con el Backend (sin errores CORS)
- [ ] Swagger UI accesible en http://localhost:8080/swagger-ui.html

---

**¡Listo para desarrollar! 🚀**

