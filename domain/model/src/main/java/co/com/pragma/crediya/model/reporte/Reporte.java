package co.com.pragma.crediya.model.reporte;
import lombok.*;
//import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor  // <- agrega constructor vacío
@AllArgsConstructor
@Builder
public class Reporte {
    private String solicitudId;
    private BigDecimal montoTotalAprobadas;
    private BigDecimal totalAprobadas;
    private String updatedAt;
}
