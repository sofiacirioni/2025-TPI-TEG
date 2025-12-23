export class Pais {
  id_pais: number;
  nombre : string;
  id_continente: number;

  constructor(id: number, name: string, id_continente: number) {
    this.id_pais = id;
    this.nombre = name;
    this.id_continente = id_continente;
  }

  //TODO:
  agregarPais(): string {
    return "";
  }
  obtenerVecinos(){

  }


}
