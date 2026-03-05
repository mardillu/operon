package com.mardillu.operon.api

import com.mardillu.operon.data.AgentStepPayload
import com.mardillu.operon.data.StructuredActionResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AgentApi {
    @POST("/agent/step")
    suspend fun processStep(
        @Body payload: AgentStepPayload
    ): StructuredActionResponse
}
