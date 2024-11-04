package uk.gov.justice.digital.hmpps.config

import com.azure.identity.ClientSecretCredentialBuilder
import com.microsoft.graph.serviceclient.GraphServiceClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GraphServiceClientConfig {
    @Bean
    fun graphServiceClient(
        @Value("\${microsoft-graph.tenant-id}") tenantId: String,
        @Value("\${microsoft-graph.client-id}") clientId: String,
        @Value("\${microsoft-graph.client-secret}") clientSecret: String,
    ): GraphServiceClient {
        val credential = ClientSecretCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build()
        return GraphServiceClient(credential, "https://graph.microsoft.com/.default")
    }
}