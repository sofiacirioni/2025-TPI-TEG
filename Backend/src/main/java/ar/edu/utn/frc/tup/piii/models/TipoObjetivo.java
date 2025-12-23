package ar.edu.utn.frc.tup.piii.models;

public enum TipoObjetivo {
    SECRETO(1),
    GENERAL(2);

    private final int id_tipo_objetivo;

    TipoObjetivo(int id_tipo_objetivo){
        this.id_tipo_objetivo = id_tipo_objetivo;
    }

    public int getIdTipoObj(){
        return id_tipo_objetivo;
    }

}
