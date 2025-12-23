package ar.edu.utn.frc.tup.piii.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

public enum FaseTurno {
    ATACAR,
    MOVER_TROPAS,
    DEFENDER
}