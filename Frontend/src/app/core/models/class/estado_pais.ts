export class EstadoPais{
  id_estado_pais: number;
  id_pais: number;
  id_jugador: number;
  id_estadísticas: number;

  constructor(id: number, id_pais: number, id_jugador: number, id_est: number) {
    this.id_estado_pais = id;
    this.id_pais = id_pais;
    this.id_jugador = id_jugador;
    this.id_estadísticas = id_est;
  }

  //TODO:COMPLETAR
  actualizarTropas(): string {
    return "";
  }

}
