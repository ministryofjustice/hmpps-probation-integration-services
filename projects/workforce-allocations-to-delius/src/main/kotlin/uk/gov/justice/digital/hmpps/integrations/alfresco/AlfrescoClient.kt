package uk.gov.justice.digital.hmpps.integrations.alfresco

import org.springframework.cloud.openfeign.FeignClient
import uk.gov.justice.digital.hmpps.config.AlfrescoFeignConfig

@FeignClient(name = "alfresco", url = "\${integrations.alfresco.url}", configuration = [AlfrescoFeignConfig::class])
interface AlfrescoClient
