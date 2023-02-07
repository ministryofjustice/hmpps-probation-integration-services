package uk.gov.justice.digital.hmpps.integrations.alfresco

import com.fasterxml.jackson.annotation.JsonAlias

data class AlfrescoDocument(
    @JsonAlias("ID")
    val id: String
)
