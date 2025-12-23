package ar.edu.utn.frc.tup.piii.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)

public class Jugador {

    private Long idJugador;
    private String nombre;
    private Objetivo objetivo;
    private Usuario usuario;
    private TipoJugador tipoJugador;
    private Color color;
    private Sala sala;
    private Partida partida;
    private boolean perdio;
    private boolean aceptoPausa;
    private boolean aceptoRenudar;
    private boolean finalizarPartida;
    //Tropas en General
    private Integer ejercito;

    private EstadoJugador estadoJugador;
    public Jugador(Long idJugador, String nombre, Usuario idUsuario, Color color, Sala sala, Integer ejercito){
        this.idJugador = idJugador;
        this.nombre = nombre;
        this.usuario = idUsuario;
        this.tipoJugador= TipoJugador.HUMANO;
        this.color = color;
        this.sala=sala;
        this.partida=null;
        this.perdio=false;
        this.aceptoPausa=false;
        this.aceptoRenudar=false;
        this.finalizarPartida=false;
        this.estadoJugador=EstadoJugador.ACTIVO;
        this.ejercito=0;

    }



}

