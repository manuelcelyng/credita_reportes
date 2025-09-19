package co.com.pragma.crediya.usecase;


import co.com.pragma.crediya.model.reporte.Reporte;
import co.com.pragma.crediya.model.reporte.gateways.ReporteGateway;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;



public class ReportesUseCase {

    private final ReporteGateway dynamoGateway;

    public ReportesUseCase(ReporteGateway dynamoGateway) {
        this.dynamoGateway = dynamoGateway;
    }

    public Mono<Void> updateReport(BigDecimal approvedAmountCents) {
        return dynamoGateway.updateReport(approvedAmountCents);
    }

    public Mono<Reporte> get() {
        return dynamoGateway.get();
    }



}
