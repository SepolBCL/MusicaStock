package ipca.example.musicastock.data.remote.api

import ipca.example.musicastock.data.remote.dto.EnvironmentStatusDto
import retrofit2.http.GET
import retrofit2.http.Path

interface EnvironmentsApi {

    @GET("api/Environments/{id}/status")
    suspend fun getEnvironmentStatus(
        @Path("id") id: String
    ): EnvironmentStatusDto
}
