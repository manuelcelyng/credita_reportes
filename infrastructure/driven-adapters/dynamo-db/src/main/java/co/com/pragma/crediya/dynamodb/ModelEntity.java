package co.com.pragma.crediya.dynamodb;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.math.BigDecimal;

/* Enhanced DynamoDB annotations are incompatible with Lombok #1932
         https://github.com/aws/aws-sdk-java-v2/issues/1932*/
@DynamoDbBean
public class ModelEntity {

    private String solicitudId;
    private BigDecimal montoTotalAprobadas;
    private BigDecimal totalAprobadas;
    private String updatedAt;




    @DynamoDbPartitionKey
    @DynamoDbAttribute("solicitud_id")
    public String getSolicitudId() {
        return solicitudId;
    }
    public void setSolicitudId( String solicitudId) {
        this.solicitudId = solicitudId;
    }

    @DynamoDbAttribute("monto_total_aprobadas")
    public BigDecimal getMontoTotalAprobadas() {
        return montoTotalAprobadas;
    }
    public void setMontoTotalAprobadas(BigDecimal montoTotalAprobadas) {
        this.montoTotalAprobadas = montoTotalAprobadas;
    }

    @DynamoDbAttribute("total_aprobadas")
    public BigDecimal getTotalAprobadas() {
        return totalAprobadas;
    }
    public void setTotalAprobadas(BigDecimal total_aprobadas) {
        this.totalAprobadas = total_aprobadas;
    }

    @DynamoDbAttribute("updatedAt")
    public String getUpdatedAt() {return updatedAt;}
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}




