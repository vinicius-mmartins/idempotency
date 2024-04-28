package com.github.viniciusmartins.idempotency

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.web.bind.annotation.*
import redis.clients.jedis.JedisPool
import redis.clients.jedis.params.SetParams


@RestController
class ByKeyController (val repo: Repo) {
    companion object { private val log = KotlinLogging.logger {} }

    @PostMapping("idempotency/key")
    fun byKey(@RequestHeader("Idempotency-Key", required = true) idempotencyKey: String,
              @RequestBody body: Payload) : String {

        // tenta salvar a chave, retorna 'OK' se existia, retorna null se não
        if (!lock(idempotencyKey, Json.encodeToString(body))) {
            log.warn { "key $idempotencyKey já utilizada!" }
            return "Error 422 : key $idempotencyKey já utilizada!"
        }
        try {
            repo.save(body)
            return "Sucesso na operação na primeira tentativa com a chave $idempotencyKey"
        } catch (e: Exception) {
            log.warn { "Erro ${e.message}, removendo chave pra poder ser retentada" }
            unlock(idempotencyKey)
            throw e
        }
    }

    @GetMapping("idempotency/key")
    fun get() : MutableList<Payload> = repo.findAll()

}

val pool = JedisPool("localhost", 6379)

const val key_ttl_seconds = 30L

fun lock(key: String, content: String) : Boolean {
    val jedis = pool.resource
    val response = jedis.set(key, content, SetParams().nx().ex(key_ttl_seconds))
    return response == "OK"
}

fun unlock(key: String) = pool.resource.use { it.del(key) }
