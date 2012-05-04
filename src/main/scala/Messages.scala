import akka.actor.ActorRef

case class MovePatches(val agent:ActorRef, val from:ActorRef, val to:ActorRef)

case class AgentLeft(val agent:ActorRef)

case class AgentEntered(val agent:ActorRef)

case class Tick()

case class TickComplete()

case class FetchAgentRefs()

case class AgentsForPatch(val agentRefs:scala.collection.immutable.Vector[ActorRef])