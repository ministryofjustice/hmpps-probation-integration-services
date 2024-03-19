package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable

@Entity
@Immutable
class Court(

    @Id
    @Column(name = "court_id")
    val id: Long,

    @Column(name = "court_name", nullable = true)
    val name: String?

)