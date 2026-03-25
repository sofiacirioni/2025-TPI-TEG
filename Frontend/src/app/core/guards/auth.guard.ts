import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  // Sin token en memoria — intentar recuperar sesión con refresh cookie
  return authService.tryRefresh().pipe(
    map(success => {
      if (success) return true;
      router.navigate(['/iniciar-sesion']);
      return false;
    })
  );
};
