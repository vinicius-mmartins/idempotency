package com.github.viniciusmartins.idempotency

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import kotlinx.serialization.Serializable

@Serializable
@Entity
class Payload (
    @Id
    @GeneratedValue
    val id: Int?=null,
    val uniqueNumber: String
)
