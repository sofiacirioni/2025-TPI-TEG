import { Component } from "@angular/core"
import { Router } from '@angular/router';
import { CommonModule } from "@angular/common"

@Component({
  selector: "app-ayuda",
  standalone: true,
  imports: [CommonModule],
  templateUrl: "./ayuda.component.html",
  styleUrls: ["./ayuda.component.css"],
})
export class AyudaComponent {
  seccionActiva = "reglas-basicas"

  constructor(private router: Router) {}

  reglasBasicas = {
    titulo: "Reglas Básicas",
    contenido: [
      {
        subtitulo: "Idea General",
        texto:
          "El juego propone un conflicto bélico que tiene lugar sobre un mapa-tablero dividido en 50 países. Cada jugador tiene un OBJETIVO SECRETO a cumplir, que se le asigna por azar y que el resto de los jugadores desconoce.",
      },
      {
        subtitulo: "Mapa",
        texto:
          "Un planisferio dividido en 50 países agrupados en 6 continentes: América del Norte, América del Sur, Europa, Asia, África y Oceanía.",
      },
      {
        subtitulo: "Jugadores",
        texto:
          "Puede ser jugado de 2 a 6 jugadores. Cada jugador elige un color de ficha que usará durante todo el partido.",
      },
      {
        subtitulo: "Objetivo Principal",
        texto:
          "Existe un objetivo común a todos los participantes: ocupar 30 países. Este objetivo es independiente del objetivo secreto, y el jugador que llega a ocupar 30 países gana automáticamente.",
      },
      {
        subtitulo: "Preparación Inicial",
        texto:
          "Se reparten las 50 tarjetas de países al azar entre los jugadores. Cada país recibe un ejército. Luego cada jugador recibe una tarjeta de objetivo secreto sin mostrarla.",
      },
    ],
  }

  fases = {
    titulo: "Fases del Juego",
    contenido: [
      {
        subtitulo: "1. Incorporación de Ejércitos",
        texto:
          "Al comenzar su turno el jugador incorpora ejércitos por: países ocupados (50% del total, mínimo 3), continentes ocupados (Asia: 7, Europa: 5, América del Norte: 5, América del Sur: 3, África: 3, Oceanía: 2), y por canje de tarjetas.",
      },
      {
        subtitulo: "2. Ataques",
        texto:
          "Un país puede atacar a otro limítrofe si dispone de ejércitos de ataque (total menos uno). Se lanzan dados: atacante máximo 3, defensor máximo 3. En empate gana el defensor.",
      },
      {
        subtitulo: "3. Reagrupamiento",
        texto:
          "Pasar ejércitos de un país a otro limítrofe propio. Se puede trasladar tantos como se quiera, dejando al menos uno de ocupación. Si se reagrupa, ya no se puede atacar.",
      },
      {
        subtitulo: "4. Solicitar Tarjeta",
        texto:
          "Si conquistó al menos un país, tiene derecho a recibir una tarjeta de países. Con menos de 3 canjes: 1 país. Con 3 o más canjes: 2 países mínimo.",
      },
    ],
  }

  canjes = {
    titulo: "Sistema de Canjes",
    contenido: [
      {
        subtitulo: "Tipos de Canje",
        texto:
          "Se puede canjear con 3 tarjetas iguales (galeón-galeón-galeón) o 3 distintas (galeón-globo-cañón). Las tarjetas con los tres símbolos actúan como comodines.",
      },
      {
        subtitulo: "Valores de Canje",
        texto:
          "1º canje: 4 ejércitos, 2º canje: 7 ejércitos, 3º canje: 10 ejércitos. De aquí en adelante se aumentan 5 ejércitos por vez (15, 20, 25...).",
      },
      {
        subtitulo: "Obligatoriedad",
        texto:
          "Mientras se tengan 5 o menos tarjetas, no hay obligación de canjear. Pero es obligatorio hacerlo antes de recibir la 6ª tarjeta.",
      },
      {
        subtitulo: "Canje Defensivo",
        texto:
          "Se puede efectuar el canje después de atacar y antes de solicitar tarjeta. Los ejércitos deben colocarse obligatoriamente en el último país conquistado.",
      },
      {
        subtitulo: "Premio por País",
        texto:
          "Cuando se posee simultáneamente un país y su tarjeta correspondiente, se pueden agregar 2 ejércitos adicionales de premio en ese país por única vez.",
      },
    ],
  }

  pactos = {
    titulo: "Pactos y Alianzas",
    contenido: [
      {
        subtitulo: "Reglas Generales",
        texto:
          "Los pactos deben ser públicos, expresados en voz alta, y sin cláusulas secretas. Cualquier jugador puede preguntar sobre los pactos existentes.",
      },
      {
        subtitulo: "Pacto entre Países",
        texto:
          "Un jugador puede proponer un pacto de no-agresión entre uno de sus países y un país limítrofe. En caso de aceptarse, ninguno atacará al otro en esa frontera.",
      },
      {
        subtitulo: "Pactos Mundiales",
        texto:
          "Los pactos de no agresión en todo el mapa están permitidos, pero no son convenientes ya que los demás jugadores concentrarían sus ataques.",
      },
      {
        subtitulo: "Zona Internacional",
        texto:
          "A partir del tercer canje, se puede proponer un pacto de Zona Internacional sobre un país limítrofe poco defendido, permitiendo conquistas alternadas.",
      },
      {
        subtitulo: "Ruptura de Pactos",
        texto:
          "Un pacto se rompe cuando: una parte anuncia su ruptura (debe respetarlo hasta el próximo turno del otro), o alguno de los países del pacto es ocupado por otro color.",
      },
    ],
  }

  misionesSecretas = {
    titulo: "Misiones Secretas",
    contenido: [
      {
        subtitulo: "Tipos de Objetivos",
        texto:
          "Existen dos tipos: Objetivos de Ocupación (ocupar territorios específicos) y Objetivos de Destrucción (eliminar un color determinado).",
      },
      {
        subtitulo: "Objetivos de Ocupación",
        texto:
          "Se cumple cuando se ocupan todos los países requeridos. No hay inconveniente en poseer un excedente de países y continentes.",
      },
      {
        subtitulo: "Objetivos de Destrucción",
        texto:
          "Se cumple al eliminar el último ejército del color objetivo del mapa. Si es imposible (color propio o inexistente), pasa a destruir al jugador de la derecha.",
      },
      {
        subtitulo: "Lista de Objetivos",
        texto:
          "1. África + 5 países de América del Norte + 4 de Europa\n2. América del Sur + 7 países de Europa + 3 limítrofes\n3. Asia + 2 países de América del Sur\n4. Europa + 4 países de Asia + 2 de América del Sur\n5. América del Norte + 2 de Oceanía + 4 de Asia\n6. 2 Oceanía + 2 África + 2 América del Sur + 3 Europa + 4 América del Norte + 3 Asia\n7. Oceanía + América del Norte + 2 Europa\n8. América del Sur + África + 4 Asia\n9. Oceanía + África + 5 América del Norte\n10-15. Destruir ejércitos de colores específicos",
      },
      {
        subtitulo: "Objetivo Común",
        texto:
          "Ocupar 30 países. Este objetivo es independiente del secreto y quien lo logre gana automáticamente, aunque su tarjeta indique algo diferente.",
      },
    ],
  }

  cambiarSeccion(seccion: string) {
    this.seccionActiva = seccion
  }

  volver() {
    window.history.back()
  }

  formatTexto(texto: string): string {
    return texto.replace(/\n/g, '<br>');
  }


}
