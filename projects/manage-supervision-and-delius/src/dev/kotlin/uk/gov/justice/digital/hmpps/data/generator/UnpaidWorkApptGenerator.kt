package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.ACTIVE_ORDER
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.UpwAppointment
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.UpwDetails

object UnpaidWorkApptGenerator {

    val UNPAID_WORK_DETAILS_1 = UpwDetails(IdGenerator.getAndIncrement(), ACTIVE_ORDER, 0)

    val APPT1 = UpwAppointment(IdGenerator.getAndIncrement(), 3, "Y", 0, UNPAID_WORK_DETAILS_1)
    val APPT2 = UpwAppointment(IdGenerator.getAndIncrement(), 4, "Y", 1, UNPAID_WORK_DETAILS_1)
    

}