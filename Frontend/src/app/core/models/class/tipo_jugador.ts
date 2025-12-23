export class TipoJugador {
  id_tipo_jugador: number;
  descripcion: string;

  constructor(id: number, des: string) {
    this.id_tipo_jugador = id;
    this.descripcion = des;
  }

  //TODO:
  esBot(){
  }

}
