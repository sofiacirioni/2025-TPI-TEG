export class Estadistica {
  id_estadistica: number;
  configuracion: string;
  estado: string;

  constructor(id: number, conf: string, estado: string) {
    this.id_estadistica = id;
    this.configuracion = conf;
    this.estado = estado;
  }

  //TODO:
  registrarEvento(): string {
    return "";
  }
  exportarJSON(){

  }



}
