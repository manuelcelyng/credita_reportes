package co.com.pragma.crediya.api.docs;

import co.com.pragma.crediya.api.Handler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

public interface ReportesControllerDocs {

    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/reportes", // La ruta del endpoint
                    produces = {MediaType.APPLICATION_PDF_VALUE},
                    method = RequestMethod.GET,
                    beanClass = Handler.class, // La clase que maneja la lógica
                    beanMethod = "Retorna el Reporte", // El método en la clase Handler
                    operation = @Operation(
                            summary = "Obtener cantidad de solicitudes aprobadas",
                            description = "Endpoint para obtener la cantidad de solicitudes que han sido aprobados.",
                            operationId = "obtenerCantidadDeAprobadas",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Reporte generado exitosamente.",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_PDF_VALUE,
                                                    schema = @Schema(
                                                            type = "string",
                                                            format = "binary"
                                                    )
                                            )
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Parámetros de solicitud inválidos."),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor."),
                                    @ApiResponse(responseCode = "403", description = "No tiene permisos para acceder a este recurso."),
                                    @ApiResponse(responseCode = "401", description = "No se ha autenticado.")
                            }
                    )
            )
    })
    RouterFunction<ServerResponse> routerFunction(Handler handler);
}