package co.com.pragma.crediya.dynamodb;



import co.com.pragma.crediya.model.reporte.Reporte;
import co.com.pragma.crediya.model.reporte.gateways.ReporteGateway;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.math.BigDecimal;
import java.util.Map;

@Repository
public class DynamoDBTemplateAdapter implements ReporteGateway {

    private final DynamoDbAsyncClient ddb;
    private final DynamoDbEnhancedAsyncClient enhanced;
    private final ObjectMapper mapper;
    private final String tableName;
    private final String singlePk;

    public DynamoDBTemplateAdapter(
            DynamoDbAsyncClient ddb,
            DynamoDbEnhancedAsyncClient enhanced,
            ObjectMapper mapper,
            @Value("${app.dynamo.table}") String table,
            @Value("${app.dynamo.pk}") String pk
    ) {
        this.ddb = ddb;
        this.enhanced = enhanced;
        this.mapper = mapper;
        this.tableName = table;
        this.singlePk = pk;
    }

    private DynamoDbAsyncTable<ModelEntity> table() {

        return enhanced.table(tableName, TableSchema.fromBean(ModelEntity.class));
    }

    /** Upsert atómico: +1 al conteo y +approvedAmountCents al total, actualiza updatedAt */
    @Override
    public Mono<Void> updateReport(BigDecimal approvedAmountCents) {

        var key = Map.of(singlePk, AttributeValue.builder().s(singlePk).build());

        var req = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .updateExpression(
                        "SET " +
                                "#total_aprobadas = if_not_exists(#total_aprobadas, :zero) + :one, " +
                                "#monto_total_aprobadas = if_not_exists(#monto_total_aprobadas, :zeroDec) + :amount, " +
                                "#updatedAt = :now"
                )
                .expressionAttributeNames(Map.of(
                        "#total_aprobadas", "total_aprobadas",
                        "#monto_total_aprobadas", "monto_total_aprobadas",
                        "#updatedAt", "updatedAt"
                ))
                .expressionAttributeValues(Map.of(
                        ":zero",   AttributeValue.builder().n("0").build(),
                        ":one",    AttributeValue.builder().n("1").build(),
                        ":zeroDec",AttributeValue.builder().n("0").build(),
                        ":amount", AttributeValue.builder().n(approvedAmountCents.stripTrailingZeros().toPlainString()).build(),
                        ":now",    AttributeValue.builder().s(java.time.Instant.now().toString()).build()
                ))
                .build();

        return Mono.fromFuture(ddb.updateItem(req)).then();
    }

    /** Lectura consistente del único registro (PK fija) */
    /** Lectura consistente del único registro (PK fija) */
    @Override
    public Mono<Reporte> get() {
        Key key = Key.builder().partitionValue(singlePk).build();
        return Mono.fromFuture(table().getItem(r -> r.key(key).consistentRead(true)))
                .flatMap(e -> e == null ? Mono.empty() : Mono.just(mapper.map(e, Reporte.class)));
    }
}