import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // No agregar token a rutas de auth
  if (req.url.includes('/auth/')) {
    return next(req);
  }

  const token = authService.getAccessToken();
  const authReq = token
    ? req.clone({ headers: req.headers.set('Authorization', `Bearer ${token}`) })
    : req;

  return next(authReq).pipe(
    catchError(error => {
      if (error.status === 401) {
        // Token expirado — intentar refresh y reintentar
        return authService.tryRefresh().pipe(
          switchMap(success => {
            if (success) {
              const newToken = authService.getAccessToken();
              const retryReq = req.clone({
                headers: req.headers.set('Authorization', `Bearer ${newToken}`)
              });
              return next(retryReq);
            }
            router.navigate(['/iniciar-sesion']);
            return throwError(() => error);
          })
        );
      }
      return throwError(() => error);
    })
  );
};
