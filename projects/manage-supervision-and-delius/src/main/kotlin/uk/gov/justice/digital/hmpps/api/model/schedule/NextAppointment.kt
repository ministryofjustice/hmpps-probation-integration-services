package uk.gov.justice.digital.hmpps.api.model.schedule

import uk.gov.justice.digital.hmpps.api.model.activity.Activity
import uk.gov.justice.digital.hmpps.api.model.user.PersonManager

data class NextAppointment(val appointment: Activity?, val personManager: PersonManager, val usernameIsCom: Boolean)