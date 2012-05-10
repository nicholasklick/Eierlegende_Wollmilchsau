package shapiro.netfauxgo

import akka.actor._
import akka.dispatch.PriorityGenerator
import akka.dispatch.UnboundedPriorityMailbox
import com.typesafe.config.Config

// We inherit, in this case, from UnboundedPriorityMailbox
// and seed it with the priority generator
class KillPrioritizerMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
  // Create a new PriorityGenerator, lower prio means more important
  PriorityGenerator {
    // 'highpriority messages should be treated first if possible
    case Die ⇒ 0

    // PoisonPill when no other left
    case PoisonPill    ⇒ 100

    // We default to 10, which is in between high and low
    case otherwise     ⇒ 10
  })