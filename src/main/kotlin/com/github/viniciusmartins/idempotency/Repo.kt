package com.github.viniciusmartins.idempotency

import com.github.viniciusmartins.idempotency.Payload
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface Repo : JpaRepository<Payload, String>
