package com.plural_pinelabs.expresscheckoutsdk.presentation.upi

import com.plural_pinelabs.expresscheckoutsdk.data.fetch.CommonApiService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.Query

class UpiInquiryApiContractTest {
    @Test
    fun `inquiry exposes active payment id query`() {
        val method = CommonApiService::class.java.declaredMethods
            .single { it.name == "statusOfTransaction" }
        val get = requireNotNull(method.getAnnotation(GET::class.java))
        val queries = method.parameterAnnotations
            .flatMap { annotations -> annotations.toList() }
            .filterIsInstance<Query>()
            .map(Query::value)

        assertEquals("inquiry", get.value)
        assertTrue(queries.contains("token"))
        assertTrue(queries.contains("order_id"))
        assertTrue(queries.contains("payment_id"))
    }
}
