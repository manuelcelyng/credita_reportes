package co.com.pragma.crediya.model.reporte.gateways;

import co.com.pragma.crediya.model.reporte.Reporte;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ReporteGateway {

    Mono<Void> updateReport(BigDecimal approvedAmountCents);
    Mono<Reporte> get();

}
