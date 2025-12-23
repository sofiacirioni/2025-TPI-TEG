export class Tarjeta {
  id_tarjeta: number;
  id_pais: number;
  id_simbolo: number;

  constructor(id: number, id2: number, id3: number) {
    this.id_tarjeta = id;
    this.id_pais = id2;
    this.id_simbolo = id3;
  }

  //TODO:
  obtenerSimbolo(): string {
    return "";
  }



}
