import {Routes} from '@angular/router';
import {FormUsuarioComponent} from '../features/Registrarse/registrarse';
import {PrincipalComponent} from '../features/Principal/principal.component';
import {PerfilUsuario} from '../features/PerfilUsuario/perfilUsuario';
import {SalaComponent} from '../features/Sala/sala.component';
import {LoginComponent} from '../features/InicioSesion/inicioSesion.component';
import {ConfigPartidaComponent} from '../features/ConfigPartida/configPartida';
import {TableroComponent} from '../features/tablero/tablero.componet';
import {EstadisticaComponent } from '../features/Estadistica/estadistica';
import {CreditosComponent} from '../features/creditos/creditos.component';
import {AyudaComponent} from '../features/Ayuda/ayuda';

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

