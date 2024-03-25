package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Immutable
class PersonalCircumstance(
    @Id
    @Column(name = "personal_circumstance_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "circumstance_type_id", updatable = false)
    val type: PersonalCircumstanceType,

    @ManyToOne
    @JoinColumn(name = "circumstance_sub_type_id", updatable = false)
    val subType: PersonalCircumstanceSubType?
)

@Entity
@Table(name = "r_circumstance_type")
@Immutable
class PersonalCircumstanceType(
    @Id
    @Column(name = "circumstance_type_id")
    val id: Long,

    @Column(name = "code_value")
    val code: String,

    @Column(name = "code_description")
    val description: String
) {
    enum class Code(val value: String) {
        CARE_LEAVER("CL"),
        VETERAN("J")
    }
}

@Entity
@Table(name = "r_circumstance_sub_type")
@Immutable
class PersonalCircumstanceSubType(
    @Id
    @Column(name = "circumstance_sub_type_id")
    val id: Long,

    @Column(name = "code_description")
    val description: String
)

interface PersonalCircumstanceRepository : JpaRepository<PersonalCircumstance, Long> {
    fun findByPersonId(personId: Long): List<PersonalCircumstance>
}