# Stage 1: Build de Angular
FROM node:20-alpine AS build
WORKDIR /app

# Copiar package files primero para cache de capas
COPY package*.json ./
RUN npm ci --legacy-peer-deps

# Copiar el resto y compilar en modo producción
COPY . .
RUN npm run build

# Stage 2: Nginx para servir los archivos estáticos
FROM nginx:1.25-alpine

# Copiar el build de Angular
# El proyecto usa el builder "browser" de angular-devkit (no "application"),
# por lo que el output va a dist/tpi-teg-fe-tpi-teg directamente
# (sin subdirectorio /browser)
COPY --from=build /app/dist/tpi-teg-fe-tpi-teg /usr/share/nginx/html

# Copiar configuración de Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
