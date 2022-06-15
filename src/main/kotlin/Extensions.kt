import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

fun JsonElement?.toBool() = this.toString().toBoolean()
fun JsonElement?.toLong() = this.toString().toLong()
fun JsonElement.getObject(value: String) = this.jsonObject[value]?.jsonObject
fun JsonElement.getArray(value: String) = this.jsonObject[value]?.jsonArray

fun <K, V : Any> MutableMap<K, V>.mergeReduceInPlace(other: Map<K, V>, reduce: (V, V) -> V): Map<K, V> =
    this.apply { other.forEach { merge(it.key, it.value, reduce) } }

fun <K, V : Any> Map<K, V>.mergeReduce(others: Map<K, V>, reduce: (V, V) -> V): Map<K, V> =
    this.toMutableMap().apply { others.forEach { merge(it.key, it.value, reduce) } }
