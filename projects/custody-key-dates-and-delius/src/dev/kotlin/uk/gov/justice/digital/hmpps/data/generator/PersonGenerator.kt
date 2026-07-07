package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

object PersonGenerator {
    val DEFAULT = Person(IdGenerator.getAndIncrement(), "A500000", "A5089DY")
    val PERSON_WITH_KEYDATES = Person(IdGenerator.getAndIncrement(), "A000001", "A0001DY")
    val PERSON_WITH_KEYDATES_BY_CRN = Person(IdGenerator.getAndIncrement(), "A000002", "A0002DY")
    val PSS_PERSON = Person(IdGenerator.getAndIncrement(), "A000003", "A0003DY")
    val SDS_PLUS_PERSON = Person(IdGenerator.getAndIncrement(), "A000004", "A0004DY")}
