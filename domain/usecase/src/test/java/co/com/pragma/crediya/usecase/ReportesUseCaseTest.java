package co.com.pragma.crediya.usecase;

import co.com.pragma.crediya.model.reporte.Reporte;
import co.com.pragma.crediya.model.reporte.gateways.ReporteGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportesUseCaseTest {

    @Mock
    private ReporteGateway gateway;

    private ReportesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReportesUseCase(gateway);
    }

    @Test
    @DisplayName("updateReport delega en el gateway y completa")
    void updateReport_delegates_and_completes() {
        BigDecimal amount = new BigDecimal("123.45");
        when(gateway.updateReport(any(BigDecimal.class))).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateReport(amount))
                .verifyComplete();

        ArgumentCaptor<BigDecimal> captor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(gateway, times(1)).updateReport(captor.capture());
        assertThat(captor.getValue()).isEqualByComparingTo(amount);
        verifyNoMoreInteractions(gateway);
    }

    @Test
    @DisplayName("updateReport propaga el error del gateway")
    void updateReport_propagates_error() {
        BigDecimal amount = new BigDecimal("10.00");
        RuntimeException boom = new RuntimeException("boom");
        when(gateway.updateReport(any(BigDecimal.class))).thenReturn(Mono.error(boom));

        StepVerifier.create(useCase.updateReport(amount))
                .expectErrorMatches(t -> t == boom)
                .verify();

        verify(gateway, times(1)).updateReport(amount);
        verifyNoMoreInteractions(gateway);
    }

    @Test
    @DisplayName("get devuelve el reporte del gateway")
    void get_returns_report_from_gateway() {
        Reporte reporte = Reporte.builder()
                .solicitudId("id-1")
                .montoTotalAprobadas(new BigDecimal("1000"))
                .totalAprobadas(new BigDecimal("3"))
                .updatedAt("2025-09-18T00:00:00Z")
                .build();
        when(gateway.get()).thenReturn(Mono.just(reporte));

        StepVerifier.create(useCase.get())
                .assertNext(r -> assertThat(r).isEqualTo(reporte))
                .verifyComplete();

        verify(gateway, times(1)).get();
        verifyNoMoreInteractions(gateway);
    }

    @Test
    @DisplayName("get propaga el error del gateway")
    void get_propagates_error() {
        IllegalStateException ex = new IllegalStateException("no data");
        when(gateway.get()).thenReturn(Mono.error(ex));

        StepVerifier.create(useCase.get())
                .expectErrorMatches(t -> t == ex)
                .verify();

        verify(gateway, times(1)).get();
        verifyNoMoreInteractions(gateway);
    }
}
