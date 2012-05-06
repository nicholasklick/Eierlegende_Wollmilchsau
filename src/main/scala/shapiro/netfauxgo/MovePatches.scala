package shapiro.netfauxgo

case class MovePatches(val agent: ActorRef, val from: ActorRef, val to: ActorRef)
