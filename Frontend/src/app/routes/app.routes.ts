import {Routes} from '@angular/router';
import {FormUsuarioComponent} from '../pages/registrarse/registrarse.component';
import {PrincipalComponent} from '../pages/principal/principal.component';
import {PerfilUsuario} from '../pages/perfil-usuario/perfil-usuario.component';
import {SalaComponent} from '../pages/sala/sala.component';
import {LoginComponent} from '../pages/inicio-sesion/inicio-sesion.component';
import {ConfigPartidaComponent} from '../pages/config-partida/config-partida.component';
import {TableroComponent} from '../pages/tablero/tablero.component';
import {EstadisticaComponent} from '../pages/estadistica/estadistica.component';
import {CreditosComponent} from '../pages/creditos/creditos.component';
import {AyudaComponent} from '../pages/ayuda/ayuda.component';

export const routes: Routes = [
  { //Redireccionamiento
    path: '',
    redirectTo: 'principal',
    pathMatch: 'full'
  },
  { //Pantalla de inicio, incluye boton de jugar
    path: 'principal',
    component: PrincipalComponent
  },
  { //Inicia la sesion
    path: 'iniciar-sesion',
    component: LoginComponent
  },
  { //Registra a un nuevo usuario
    path: 'registrarse',
    component: FormUsuarioComponent
  },
  { //Muestra los datos del usuario
    path: 'perfilUsuario',
    component: PerfilUsuario
  },
  { // Crear/unirse a sala/unirse a partida
    path: 'entrarCrearSala',
    component: SalaComponent
  },
  { //Lobby
    path: 'configPartida',
    component: ConfigPartidaComponent
  },
  {path:'creditos', component: CreditosComponent},
  {
    path: 'estadisticas',
    component: EstadisticaComponent
  },
  { // Juego principal (tablero, botones, info etc)
    path: 'juego/:url',
    component: TableroComponent
  },
  { //Pantalla de ayuda
    path: 'ayuda',
    component: AyudaComponent
  },
  { //Rebote para
    path: '**',
    redirectTo: 'principal'
  }
]

