import { Injectable } from '@angular/core';

// Re-exportado por compatibilidad con componentes que lo importaban desde aquí
export type { UserInfo as UsuarioDto } from '../models/interfaces/auth.interfaces';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { AuthResponse, RefreshResponse, RegisterRequest, UserInfo } from '../models/interfaces/auth.interfaces';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authUrl = `${environment.apiUrl}/auth`;

  // Access token guardado en memoria — nunca en localStorage
  private accessToken: string | null = null;

  private currentUserSubject = new BehaviorSubject<UserInfo | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(correo: string, contrasenia: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.authUrl}/login`,
      { correo, contrasenia },
      { withCredentials: true }
    ).pipe(
      tap(response => this.handleAuthResponse(response))
    );
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.authUrl}/register`,
      data,
      { withCredentials: true }
    ).pipe(
      tap(response => this.handleAuthResponse(response))
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.authUrl}/logout`, {}, { withCredentials: true }).pipe(
      tap(() => this.clearAuth()),
      catchError(() => { this.clearAuth(); return of(undefined); })
    );
  }

  // Llamar al iniciar la app — recupera sesión si el refresh token (HttpOnly) sigue válido
  tryRefresh(): Observable<boolean> {
    return this.http.post<RefreshResponse>(
      `${this.authUrl}/refresh`, {},
      { withCredentials: true }
    ).pipe(
      tap(response => {
        this.accessToken = response.accessToken;
        const user = this.decodeToken(response.accessToken);
        this.currentUserSubject.next(user);
      }),
      map(() => true),
      catchError(() => {
        this.clearAuth();
        return of(false);
      })
    );
  }

  getAccessToken(): string | null {
    return this.accessToken;
  }

  isAuthenticated(): boolean {
    return this.accessToken !== null;
  }

  getCurrentUser(): UserInfo | null {
    return this.currentUserSubject.value;
  }

  /** @deprecated usar getCurrentUser() */
  getUsuario(): UserInfo | null {
    return this.getCurrentUser();
  }

  private handleAuthResponse(response: AuthResponse): void {
    this.accessToken = response.accessToken;
    this.currentUserSubject.next({
      idUsuario: response.idUsuario,
      usuario:   response.usuario,
      correo:    response.correo,
      imagen:    response.imagen
    });
  }

  private clearAuth(): void {
    this.accessToken = null;
    this.currentUserSubject.next(null);
  }

  private decodeToken(token: string): UserInfo | null {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return { idUsuario: 0, usuario: payload.sub, correo: payload.sub, imagen: '' };
    } catch {
      return null;
    }
  }
}
