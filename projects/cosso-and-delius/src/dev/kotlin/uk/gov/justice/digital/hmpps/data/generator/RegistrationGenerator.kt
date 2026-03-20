package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Deregistration
import uk.gov.justice.digital.hmpps.entity.RegisterType
import uk.gov.justice.digital.hmpps.entity.Registration
import java.time.LocalDate

object RegistrationGenerator {
    val DEFAULT_REGISTER_TYPE = RegisterType(
        id = IdGenerator.getAndIncrement(),
        code = "ALT7",
        description = "Suicide self harm registration"
    )

    val OTHER_REGISTER_TYPE = RegisterType(
        id = IdGenerator.getAndIncrement(),
        code = "CD19",
        description = "Other registration"
    )

    val DEFAULT_OTHER_REGISTRATION_ID = IdGenerator.getAndIncrement()

    var defaultOtherRegistration: Registration? = null
    var defaultOtherDeregistration: Deregistration? = null

    init {
        defaultOtherRegistration = Registration(
            id = DEFAULT_OTHER_REGISTRATION_ID,
            person = PersonGenerator.DEFAULT_PERSON,
            notes = "Some registration notes",
            startDate = LocalDate.now().minusDays(20),
            deregistration = null, // will be set after creation
            softDeleted = false,
            deregistered = false,
            documentLinked = true,
            type = OTHER_REGISTER_TYPE,
            registerLevel = ReferenceDataGenerator.DEFAULT_REGISTER_LEVEL,
            registerCategory = ReferenceDataGenerator.DEFAULT_REGISTER_CATEGORY
        )
        defaultOtherDeregistration = Deregistration(
            id = IdGenerator.getAndIncrement(),
            registration = defaultOtherRegistration!!,
            date = LocalDate.now()
        )
        defaultOtherRegistration!!.deregistration = defaultOtherDeregistration
    }

    val DEFAULT_OTHER_REGISTRATION: Registration get() = defaultOtherRegistration!!
    val DEFAULT_OTHER_DEREGISTRATION: Deregistration get() = defaultOtherDeregistration!!

    val DEFAULT_ALT7_REGISTRATION = Registration(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT_PERSON,
        notes = "Some registration notes",
        startDate = LocalDate.now().minusDays(20),
        deregistration = null,
        softDeleted = false,
        deregistered = false,
        documentLinked = true,
        type = DEFAULT_REGISTER_TYPE,
        registerLevel = ReferenceDataGenerator.DEFAULT_REGISTER_LEVEL,
        registerCategory = ReferenceDataGenerator.DEFAULT_REGISTER_CATEGORY
    )
}
