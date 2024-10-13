package com.flip.skateshop.security

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.types.ObjectId
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Service

@Service
class SecurityUtils {
    suspend fun getCurrentUserIdOrNull(): ObjectId? {
        val context = ReactiveSecurityContextHolder.getContext().awaitFirstOrNull()
        return context?.authentication?.name?.let { ObjectId(it) }
    }

    suspend fun getCurrentUserId(): ObjectId {
        return getCurrentUserIdOrNull()!!
    }
}
