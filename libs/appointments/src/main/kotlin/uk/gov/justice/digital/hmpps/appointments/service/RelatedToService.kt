//package uk.gov.justice.digital.hmpps.appointments.service
//
//import org.springframework.data.repository.findByIdOrNull
//import org.springframework.stereotype.Service
//import org.springframework.transaction.annotation.Transactional
//import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities
//import uk.gov.justice.digital.hmpps.appointments.model.ReferencedEntities
//import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentsRepositories
//
//@Transactional
//@Service
//class RelatedToService(
//    private val personRepository: AppointmentsRepositories.PersonRepository,
//    private val eventRepository: AppointmentsRepositories.EventRepository,
//    private val requirementRepository: AppointmentsRepositories.RequirementRepository,
//) {
//    fun findRelatedTo(relatedTo: ReferencedEntities): RelatedTo {
//        val person = personRepository.getPerson(relatedTo.person.id)
//        val event = relatedTo.event?.id?.let { eventRepository.findByIdOrNull(it) }
//        val requirement = relatedTo.requirement?.id?.let { requirementRepository.findByIdOrNull(it) }
//        return RelatedTo(person, event, requirement)
//    }
//}
//
//class RelatedTo(
//    val person: AppointmentEntities.Person,
//    val event: AppointmentEntities.Event?,
//    val requirement: AppointmentEntities.Requirement?,
//)