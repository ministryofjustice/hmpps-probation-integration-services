package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Nsi
import uk.gov.justice.digital.hmpps.entity.NsiStatus
import uk.gov.justice.digital.hmpps.entity.NsiType
import java.time.ZonedDateTime

object NsiGenerator {
    val OPD_TYPE = NsiType(
        id = IdGenerator.getAndIncrement(),
        code = "OPD1",
    )

    val OPD_STATUS = NsiStatus(
        id = IdGenerator.getAndIncrement(),
        code = "OPD01",
        description = "OPD Screened In",
    )

    val OPD_NSI = Nsi(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.PERSON1,
        type = OPD_TYPE,
        status = OPD_STATUS,
        notes = "OPD pathway notes",
        createdDatetime = ZonedDateTime.parse("2024-07-01T09:00:00+01:00"),
        active = true,
        softDeleted = false,
    )
}

