package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Court
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProbationAreaEntity
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

object CourtGenerator {

    val PROBATIONARE_AREA = ProbationAreaEntity(
        true,
        "West Midlands Region",
        "N52",
        null,
        false,
        IdGenerator.getAndIncrement(),
    )

    val BHAM = Court(
        IdGenerator.getAndIncrement(),
        "BRMNCC",
        true,
        "Birmingham Crown Court",
        "0121 111 2222",
        "0121 333 4444",
        "Queen Elizabeth II Law Courts",
        "1 Newton Street",
        null,
        "Birmingham",
        "West Midlands",
        "B4 7NA",
        "England",
        ReferenceDataGenerator.CRN.id,
        ZonedDateTime.of(LocalDateTime.now().minusDays(7), ZoneId.of("Europe/London")),
        ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("Europe/London")),
        PROBATIONARE_AREA.id,
        null,
        PROBATIONARE_AREA,
        ReferenceDataGenerator.CRN
    )
}