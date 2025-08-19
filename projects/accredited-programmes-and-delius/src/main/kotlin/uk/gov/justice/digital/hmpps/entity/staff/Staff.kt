package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.ProbationPractitioner

@Entity
@Immutable
class Staff(
    @Id
    @Column(name = "staff_id")
    val id: Long,

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    val forename: String,

    val surname: String,

    @OneToOne(mappedBy = "staff")
    val user: User?,
) {
    fun toProbationPractitioner(getEmailAddress: (u: User) -> String?) =
        ProbationPractitioner(Name(forename, null, surname), code, user?.let { getEmailAddress(user) })
}