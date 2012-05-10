package shapiro.netfauxgo

class MurderousSpacerAgent(world: World) extends MovableAgent(world) {

  def tick() = {
    val otherGuys = getOtherAgentsInVicinity(1)
    if (otherGuys.length > 1) {
      otherGuys.foreach( og => killAgent(og) )
    }
  }
}