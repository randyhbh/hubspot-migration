import domain.DealEngagements
import domain.EngagementAttachments
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("Main")

fun log(msg: String?) {
    log.info(msg)
}

fun logDealEngagements(deal: DealEngagements) {
    if (deal.engagementsIds.isEmpty()) log.info("No engagements for deal $deal")
    log.info("deal: ${deal.dealId} loaded engagements: ${deal.engagementsIds}")
}

fun logEngagementAttachments(engagement: EngagementAttachments) {
    if (engagement.attachments.isEmpty()) log.info("No attachments found for engagement $engagement")
    log.info("engagement: ${engagement.id} loaded attachments: ${engagement.attachments}")
}