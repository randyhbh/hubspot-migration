package actions

import dtos.PipeLineResponse
import httpResponse
import io.ktor.client.call.*
import pipelines

suspend fun getPipelines() {
    pipelines += httpResponse("https://api.hubapi.com/crm-pipelines/v1/pipelines/deals").body<PipeLineResponse>().pipelines
}