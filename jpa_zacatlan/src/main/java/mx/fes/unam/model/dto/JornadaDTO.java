package mx.fes.unam.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//Estado de las ventas del día de un vendedor
@Data
@NoArgsConstructor
public class JornadaDTO {
    LocalDateTime inicio;
    LocalDateTime fin;

    public boolean isSinIniciar() { return inicio == null; }
    public boolean isAbierta()    { return inicio != null && fin == null; }
    public boolean isCerrada()    { return fin != null; }

    public String getInicioTexto() { return inicio == null ? "" : inicio.format(DateTimeFormatter.ofPattern("HH:mm")); }
    public String getFinTexto()    { return fin == null ? "" : fin.format(DateTimeFormatter.ofPattern("HH:mm")); }
}
