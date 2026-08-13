// Environment configuration for production
//
// Rutas relativas a propósito: en producción el frontend lo sirve el mismo
// Nginx que proxya /api y /ws al backend (ver Frontend/nginx.conf), así que
// resuelven contra el origen actual y el stack funciona en cualquier host
// (localhost, IP de red, dominio real) sin recompilar.
export const environment = {
  production: true,
  apiUrl: '/api/v1',
  wsUrl: '/ws'
};
