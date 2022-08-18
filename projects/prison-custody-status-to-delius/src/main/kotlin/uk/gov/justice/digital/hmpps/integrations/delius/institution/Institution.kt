package uk.gov.justice.digital.hmpps.integrations.delius.institution

import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Immutable
@Entity
@Table(name = "r_institution")
class Institution(
    @Id
    @Column(name = "institution_id")
    val id: Long,

    @Column
    val code: String,

    @Column(name = "nomis_cde_code", columnDefinition = "CHAR(6)")
    val nomisCdeCode: String,

    @Column
    @Type(type = "yes_no")
    var establishment: Boolean,

    @Column
    @Type(type = "yes_no")
    val selectable: Boolean,
)
