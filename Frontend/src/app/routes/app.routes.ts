import { Routes } from '@angular/router';
import { IntroComponent } from '../pages/intro/intro.component';
import { FormUsuarioComponent } from '../pages/registrarse/registrarse.component';
import { PrincipalComponent } from '../pages/principal/principal.component';
import { PerfilUsuario } from '../pages/perfil-usuario/perfil-usuario.component';
import { SalaComponent } from '../pages/sala/sala.component';
import { LoginComponent } from '../pages/inicio-sesion/inicio-sesion.component';
import { ConfigPartidaComponent } from '../pages/config-partida/config-partida.component';
import { TableroComponent } from '../pages/tablero/tablero.component';
import { EstadisticaComponent } from '../pages/estadistica/estadistica.component';
import { CreditosComponent } from '../pages/creditos/creditos.component';
import { AyudaComponent } from '../pages/ayuda/ayuda.component';
import { authGuard } from '../core/guards/auth.guard';

export const routes: Routes = [
  { path: '', component: IntroComponent },
  { path: 'iniciar-sesion', component: LoginComponent },
  { path: 'registrarse', component: FormUsuarioComponent },
  { path: 'ayuda', component: AyudaComponent },
  { path: 'creditos', component: CreditosComponent },

  // Rutas protegidas
  { path: 'principal', component: PrincipalComponent, canActivate: [authGuard] },
  { path: 'perfilUsuario', component: PerfilUsuario, canActivate: [authGuard] },
  { path: 'entrarCrearSala', component: SalaComponent, canActivate: [authGuard] },
  { path: 'configPartida', component: ConfigPartidaComponent, canActivate: [authGuard] },
  // El parte de campaña es de UNA partida: lleva su id para que sobreviva a un F5.
  { path: 'estadisticas/:idPartida', component: EstadisticaComponent, canActivate: [authGuard] },
  { path: 'estadisticas', component: EstadisticaComponent, canActivate: [authGuard] },
  { path: 'juego/:url', component: TableroComponent, canActivate: [authGuard] },

  { path: '**', redirectTo: 'principal' }
];
