export class Jugador{
  id_jugador: number;
  nombre: string;
  id_objetivo: number;
  id_usuario: number;
  id_tipo_jugador: number;
  perdio: boolean;
  id_color: number;

  constructor(id: number, name: string, id_o: number, id_user: number, id_tp: number, p: boolean, id_color: number) {
    this.id_jugador = id;
    this.nombre = name;
    this.id_objetivo = id_o;
    this.id_usuario = id_user;
    this.id_tipo_jugador = id_tp;
    this.perdio = p;
    this.id_color = id_color;

  }

  //TODO:COMPLETAR
  moverTropa(): string {
    return "";
  }
  crearPais(){

  }
  asegurarTropas(){

  }
  asignarTropa(){

  }
  revisarObjetivo(){

  }
  canjear(){

  }
  tienePacto(){

  }
  asignarColor(){

  }
  haPerdido(){

  }
  esInactivo(){

  }


}
