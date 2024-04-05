package uk.gov.justice.digital.hmpps.api.model.sentence

import uk.gov.justice.digital.hmpps.api.model.Name

data class SentenceOverview(
    val name: Name,
    val sentences: List<Sentence>,
    val probabtionHistory: ProbabtionHistory,
)
