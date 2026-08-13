export class EstadoTarjeta{
  id_estado_tarjeta: number;
  id_tarjeta: number;
  id_jugador: number;
  id_turno: number;

  constructor(id: number, id_tarjeta: number, id_jugador: number, id_turno: number) {
    this.id_estado_tarjeta = id;
    this.id_tarjeta = id_tarjeta;
    this.id_jugador = id_jugador;
    this.id_turno = id_turno;
  }

  //TODO:COMPLETAR
  asignarTarjeta(): string {
    return "";
  }
  obtenerEstadoTarjeta(){

  }

}
