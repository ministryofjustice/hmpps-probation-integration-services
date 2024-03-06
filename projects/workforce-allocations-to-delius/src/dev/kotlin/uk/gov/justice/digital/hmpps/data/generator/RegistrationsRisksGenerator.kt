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
    val RED = "red"
    val ROSH_REF_DATA = generateReferenceData("1", "RoSH")
    val ALERTS_REF_DATA = generateReferenceData("2", "Alerts")

    fun generateReferenceData() :List<Registration> = listOf(
        generateRegistration(generateRegisterType(ROSH, RED, ROSH_REF_DATA)),
        generateRegistration(generateRegisterType(ALERTS, RED, ALERTS_REF_DATA))
    )

    private fun generateRegisterType(code: String,
        colour: String,
        flag: ReferenceData,
        id: Long = IdGenerator.getAndIncrement(),) = RegisterType(code, flag, "$code description", colour, id)

    fun generateRegistration(type: RegisterType) = Registration(person = PersonGenerator.DEFAULT,
        type = type,
        date = LocalDate.now(),
        id = IdGenerator.getAndIncrement(),
        softDeleted = false,
        deRegistered = false,
        createdDateTime = ZonedDateTime.now())

    fun generateReferenceData(code: String,
        description: String,
        id: Long = IdGenerator.getAndIncrement(),
        risk: Dataset = DatasetGenerator.REGISTER_TYPE_FLAG)
        = ReferenceData(id, code, description, risk)

}