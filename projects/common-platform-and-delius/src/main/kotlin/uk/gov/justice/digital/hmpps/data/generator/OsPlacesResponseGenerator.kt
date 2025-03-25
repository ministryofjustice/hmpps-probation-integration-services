package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.client.OsPlacesResponse
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object OsPlacesResponseGenerator {
    val NO_RESULTS = ResourceLoader.file<OsPlacesResponse>("address-lookup-no-results")
    val SINGLE_RESULT = ResourceLoader.file<OsPlacesResponse>("address-lookup-single-result")
}