package co.com.pragma.crediya.api;

import co.com.pragma.crediya.usecase.ReportesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class Handler {

    final private ReportesUseCase myUseCase;

    public Mono<ServerResponse> listenGETUseCase(ServerRequest serverRequest) {
        return myUseCase.get().
                switchIfEmpty(Mono.error(new RuntimeException("No se encontraron registros."))).
                flatMap(reporte -> ServerResponse.ok().bodyValue(Map.of("TotalAprobadas",reporte)))
                .doOnError(ex -> log.error("[GET REPORTE] Error: {}", ex.toString()));
    }
}
