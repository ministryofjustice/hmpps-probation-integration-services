package uk.gov.justice.digital.hmpps.api.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.User
import uk.gov.justice.digital.hmpps.entity.UserRepository

@RestController
@PreAuthorize("hasRole('PROBATION_API__SUBJECT_ACCESS_REQUEST__DETAIL')")
class UserController(private val userRepository: UserRepository) {
    @GetMapping("/user")
    fun getUsers() = userRepository.findAll().map { User(it.username, it.surname) }
}
