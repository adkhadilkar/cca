package com.architectprep.app.data.content

import android.content.Context
import kotlinx.serialization.json.Json

class GuideRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun load(packId: String = "ccar-f"): GuideDto {
        val text = context.assets.open("content/$packId/guide.json").bufferedReader().use { it.readText() }
        return json.decodeFromString(GuideDto.serializer(), text)
    }
}
