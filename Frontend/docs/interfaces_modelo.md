
# Interfaces del Modelo

---

## IObjetivoService

```java
public interface IObjetivoService {
    void prepararObjetivos();
    void resetearObjetivos();
}
```

---

## IUsuarioService

```java
public interface IUsuarioService {
    void crearUsuario();
    void cargarUsuario();
    void editarUsuario();
    void eliminarUsuario();
}
```

---

## IEstadisticaService

```java
public interface IEstadisticaService {
    void registrarEvento();
    void exportarJSON();
}
```

---

## IEstadoPaisService

```java
public interface IEstadoPaisService {
    void actualizarTropas();
}
```

---

## IJugadorService

```java
public interface IJugadorService {
    void moverTropas();
    void atacarPais();
    void reagruparTropas();
    void asignarTropas();
    boolean revisarObjetivo();
    void capturar();
    void tieneCartas();
    void asignarColor();
    boolean haPerdido();
    boolean estaActivo();
}
```

---

## IPartidaService

```java
public interface IPartidaService {
    void crearPartida();
    void cargarPartida();
}
```

---

## ITarjetaService

```java
public interface ITarjetaService {
    void obtenerSimbolo();
}
```

---

## IEstadoTarjetaService

```java
public interface IEstadoTarjetaService {
    void asignarTarjeta();
    void obtenerEstadoTarjeta();
}
```

---

## ITurnoService

```java
public interface ITurnoService {
    void obtenerTurno();
}
```

---

## IFaseTurnoService

```java
public interface IFaseTurnoService {
    void obtenerFaseTurno();
}
```
