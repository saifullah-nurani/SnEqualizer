package com.nurani.audio.datastore

import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

internal object EqualizerPreferencesSerializer : Serializer<EqualizerPreferences> {
    override val defaultValue: EqualizerPreferences
        get() = EqualizerPreferences()

    private val json: Json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun readFrom(input: InputStream): EqualizerPreferences {
        return try {
            json.decodeFromString(
                deserializer = EqualizerPreferences.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: EqualizerPreferences, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                json.encodeToString(EqualizerPreferences.serializer(), t).encodeToByteArray()
            )
        }
    }
}