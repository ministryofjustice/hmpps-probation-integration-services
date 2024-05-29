package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.CURRENT_SENTENCE
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.UpwAppointment
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.UpwDetails

object UnpaidWorkGenerator {

    val UNPAID_WORK_DETAILS_1 = UpwDetails(IdGenerator.getAndIncrement(), CURRENT_SENTENCE, 0)

    val APPT1 = UpwAppointment(IdGenerator.getAndIncrement(), 3, "Y", "Y", 0, UNPAID_WORK_DETAILS_1)
    val APPT2 = UpwAppointment(IdGenerator.getAndIncrement(), 4, "Y", "Y", 1, UNPAID_WORK_DETAILS_1)
    val APPT3 = UpwAppointment(IdGenerator.getAndIncrement(), 0, "N", "N", 1, UNPAID_WORK_DETAILS_1)
    val APPT4 = UpwAppointment(IdGenerator.getAndIncrement(), 0, "N", "Y", 1, UNPAID_WORK_DETAILS_1)
    val APPT5 = UpwAppointment(IdGenerator.getAndIncrement(), 0, "N", "Y", 1, UNPAID_WORK_DETAILS_1)
    val APPT6 = UpwAppointment(IdGenerator.getAndIncrement(), 0, null, null, 1, UNPAID_WORK_DETAILS_1)
    val APPT7 = UpwAppointment(IdGenerator.getAndIncrement(), 0, "Y", "Y", 0, UNPAID_WORK_DETAILS_1)
}