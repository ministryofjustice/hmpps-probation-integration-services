package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.registration.entity.Registration
import java.time.LocalDate
import java.time.ZonedDateTime

object RegistrationsRisksGenerator {

    val ROSH = "Rosh"
    val ALERTS = "Alerts"
    val SAFEGUARDING = "Safeguarding"
    val INFORMATION = "Information"
    val PUBLIC_PROTECTION = "Public Protection"

    val RED = "red"
    val AMBER = "amber"
    val GREEN = "green"
    val WHITE = "white"

    val ROSH_REF_DATA = generateReferenceData("1", ROSH)
    val ALERTS_REF_DATA = generateReferenceData("2", ALERTS)
    val SAFEGUARDING_REF_DATA = generateReferenceData("3", SAFEGUARDING)
    val INFORMATION_REF_DATA = generateReferenceData("4", INFORMATION)
    val PUBLIC_PROTECTION_REF_DATA = generateReferenceData("5", PUBLIC_PROTECTION)

    val REGISTRATION_NO_REFERENCE_DATA =
        generateRegistration(
            RegisterType(code ="blank", flag = null, description = "blank", colour = "red", id = IdGenerator.getAndIncrement())
        )

    fun generateRegistrations() :List<Registration> = listOf(
        generateRegistration(generateRegisterType(ROSH, AMBER, ROSH_REF_DATA)),
        generateRegistration(generateRegisterType(ROSH, RED, ROSH_REF_DATA)),
        generateRegistration(generateRegisterType(ALERTS, GREEN, ALERTS_REF_DATA)),
        generateRegistration(generateRegisterType(ALERTS, RED, ALERTS_REF_DATA)),
        generateRegistration(generateRegisterType(SAFEGUARDING, WHITE, SAFEGUARDING_REF_DATA)),
        generateRegistration(generateRegisterType(SAFEGUARDING, RED, SAFEGUARDING_REF_DATA)),
        generateRegistration(generateRegisterType(INFORMATION, RED, INFORMATION_REF_DATA)),
        generateRegistration(generateRegisterType(PUBLIC_PROTECTION, RED, PUBLIC_PROTECTION_REF_DATA))
    )

    fun generateReferenceData(code: String,
        description: String,
        id: Long = IdGenerator.getAndIncrement(),
        risk: Dataset = DatasetGenerator.REGISTER_TYPE_FLAG)
        = ReferenceData(id, code, description, risk)

    private fun generateRegisterType(code: String,
        colour: String,
        flag: ReferenceData,
        id: Long = IdGenerator.getAndIncrement()) = RegisterType(code, flag, "$code description", colour, id)

    fun generateRegistration(type: RegisterType) = Registration(person = PersonGenerator.DEFAULT,
        type = type,
        date = LocalDate.now(),
        id = IdGenerator.getAndIncrement(),
        softDeleted = false,
        deRegistered = false,
        createdDateTime = ZonedDateTime.now())
}