package shapiro.netfauxgo

import akka.actor.ActorRef

case class KillAgent(val killer:ActorRef, val target:ActorRef)

case class AddAgent(val agent:ActorRef)

case class MovePatches(val agent:ActorRef, val from:ActorRef, val to:ActorRef)

case class AgentLeft(val agent:ActorRef)

case class AgentEntered(val agent:ActorRef)

case class Tick(val snapshot:WorldSnapshot)

case object TickComplete

case object FetchAgentRefs

case class AgentsForPatch(val agentRefs:List[ActorRef])

case class KillSucceeded(val target:ActorRef, val deadGuyState:Map[Any,Any])

case class PatchSnapshotM(val pS:PatchSnapshot)

case class AgentSnapshotM(val aS:AgentSnapshot)

case object SnapshotRequest
