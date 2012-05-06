import akka.actor.ActorRef

case class AddAgent(val agent:ActorRef)

case class MovePatches(val agent:ActorRef, val from:ActorRef, val to:ActorRef)

case class AgentLeft(val agent:ActorRef)

case class AgentEntered(val agent:ActorRef)

case object Tick

case object TickComplete

case object FetchAgentRefs

case class AgentsForPatch(val agentRefs:List[ActorRef])