package converters

import domain.Attachment
import domain.EngagementAttachments
import dtos.DealsResponse

@JvmName("toMapDealsResponse")
fun List<DealsResponse>.toMap(): Map<Long, Set<Long>> =
    this
        .map { it.deals }
        .flatten()
        .groupBy(keySelector = { it.properties.pipeline.id }, valueTransform = { it.id })
        .mapValues { it.value.toSet() }

@JvmName("toMapEngagementAttachments")
fun List<EngagementAttachments>.toMap(): Map<Long, Set<Attachment>> =
    this
        .groupBy(keySelector = { it.id }, valueTransform = { it.attachments })
        .mapValues { it.value.flatten().toSet() }
