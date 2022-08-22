package uk.gov.justice.digital.hmpps.integrations.delius.institution

import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Type
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Immutable
@Entity
@Table(name = "r_institution")
class Institution(
    @EmbeddedId
    val id: InstitutionId,

    @Column(nullable = false, columnDefinition = "char(6)")
    val code: String,

    @Column(name = "nomis_cde_code", columnDefinition = "char(6)")
    val nomisCdeCode: String,

    @Column(nullable = false)
    val description: String,

    @Column
    @Type(type = "yes_no")
    val selectable: Boolean,
)

@Embeddable
data class InstitutionId(
    @Column(name = "institution_id")
    val institutionId: Long,

    @Column(name = "establishment")
    @Type(type = "yes_no")
    val establishment: Boolean,
) : Serializable
