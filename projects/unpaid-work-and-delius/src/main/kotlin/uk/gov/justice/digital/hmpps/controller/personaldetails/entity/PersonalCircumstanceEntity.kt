package uk.gov.justice.digital.hmpps.controller.personaldetails.entity

import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import uk.gov.justice.digital.hmpps.integrations.common.entity.PersonalCircumstanceBase
import uk.gov.justice.digital.hmpps.integrations.common.entity.PersonalCircumstanceSubType
import uk.gov.justice.digital.hmpps.integrations.common.entity.PersonalCircumstanceType
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0 and (end_date is null or end_date > current_date)")
@Table(name = "personal_circumstance")
class PersonalCircumstanceEntity(
    id: Long,
    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,
    type: PersonalCircumstanceType,
    subType: PersonalCircumstanceSubType?,
    notes: String? = null,
    start: LocalDate? = null,
    endDate: LocalDate? = null,
    softDeleted: Boolean = false,
    evidenced: Boolean = false,
) : PersonalCircumstanceBase(id, type, subType, notes, start, endDate, softDeleted, evidenced)
