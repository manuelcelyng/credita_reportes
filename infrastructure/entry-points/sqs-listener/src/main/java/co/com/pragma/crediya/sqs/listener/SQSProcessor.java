package co.com.pragma.crediya.sqs.listener;


import co.com.pragma.crediya.sqs.listener.dto.MontoDTO;
import co.com.pragma.crediya.usecase.ReportesUseCase;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.math.BigDecimal;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {
    private final ReportesUseCase myUseCase;
    private final ObjectMapper objectMapper;


    @Override
    public Mono<Void> apply(Message message) {
        System.out.println(message.body());
        var body = message.body();
        MontoDTO dto = null;
        try {
            dto = objectMapper.readValue(body, MontoDTO.class);
            BigDecimal monto = dto.monto();
            myUseCase.updateReport(monto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return Mono.empty();
    }


}
