package shapiro.netfauxgo

import akka.actor.ActorRef

class MurderousSpacerAgent(world: World) extends MovableAgent(world) {

  def tick() = {
    val otherGuys = getOtherAgentsInVicinity(1)
    if (otherGuys.length > 1) {
      otherGuys.foreach( og => killAgent(og) )
    }
  }
}