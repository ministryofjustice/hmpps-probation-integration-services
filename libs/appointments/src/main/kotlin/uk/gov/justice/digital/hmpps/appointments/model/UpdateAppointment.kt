package uk.gov.justice.digital.hmpps.appointments.model

import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.AppointmentContact
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

class UpdateAppointment {
    data class Outcome(
        val outcomeCode: String?,
    ) {
        internal constructor(entity: AppointmentContact) : this(entity.outcome?.code)
    }

    data class Schedule(
        val date: LocalDate,
        val startTime: LocalTime,
        val endTime: LocalTime?,
        val allowConflicts: Boolean = false,
    ) {
        val isFuture: Boolean = date.atTime(endTime ?: startTime).atZone(EuropeLondon) > ZonedDateTime.now(EuropeLondon)

        init {
            require(endTime == null || startTime < endTime) {
                "Start time must be before end time"
            }
        }

        internal constructor(entity: AppointmentContact) : this(
            date = entity.date,
            startTime = entity.startTime.toLocalTime(),
            endTime = entity.endTime?.toLocalTime(),
        )

        internal constructor(recreate: Recreate) : this(
            date = recreate.date,
            startTime = recreate.startTime,
            endTime = recreate.endTime,
            allowConflicts = recreate.allowConflicts
        )

        infix fun isSameDateAndTimeAs(other: Schedule) =
            date == other.date && startTime == other.startTime && endTime == other.endTime

        internal infix fun isSameDateAndTimeAs(other: AppointmentContact) =
            this isSameDateAndTimeAs Schedule(other)
    }

    data class Recreate(
        val date: LocalDate,
        val startTime: LocalTime,
        val endTime: LocalTime?,
        val allowConflicts: Boolean = false,
        val rescheduledBy: RescheduledBy? = null,
        val newReference: String? = null,
    ) {
        init {
            require(endTime == null || startTime < endTime) {
                "Start time must be before end time"
            }
        }

        internal constructor(entity: AppointmentContact) : this(
            date = entity.date,
            startTime = entity.startTime.toLocalTime(),
            endTime = entity.endTime?.toLocalTime(),
        )

        enum class RescheduledBy(val outcomeCode: String) {
            PERSON_ON_PROBATION("RSOF"),
            PROBATION_SERVICE("RSSR"),
        }

        internal infix fun isSameDateAndTimeAs(other: AppointmentContact) =
            Schedule(this) isSameDateAndTimeAs Schedule(other)
    }

    data class Assignee(
        val staffCode: String,
        val teamCode: String,
        val locationCode: String?,
    ) {
        internal constructor(entity: AppointmentContact) : this(
            staffCode = entity.staff.code,
            teamCode = entity.team.code,
            locationCode = entity.officeLocation?.code
        )
    }

    data class Flags(
        val alert: Boolean? = null,
        val sensitive: Boolean? = null,
        val rarActivity: Boolean? = null,
        val visor: Boolean? = null,
    ) {
        internal constructor(entity: AppointmentContact) : this(
            alert = entity.alert,
            sensitive = entity.sensitive,
            rarActivity = entity.rarActivity,
            visor = entity.visorContact
        )
    }
}
