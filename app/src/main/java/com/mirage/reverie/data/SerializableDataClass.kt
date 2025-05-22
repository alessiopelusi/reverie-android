package com.mirage.reverie.data

import android.os.Parcelable
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

abstract class SerializableDataClass

inline fun <reified T : SerializableDataClass> T.toFirestoreMap(): Map<String, Any> {
    val moshi = Moshi.Builder()
        .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()

    // Usa il tipo concreto con `reified`
    val adapter = moshi.adapter(T::class.java)
    val jsonString = adapter.toJson(this)

    // Adapter per convertire in una mappa
    val mapAdapter: JsonAdapter<Map<String, Any>> = moshi.adapter(
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    )

    return mapAdapter.fromJson(jsonString) ?: emptyMap()
}
