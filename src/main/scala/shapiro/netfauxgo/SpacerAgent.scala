package shapiro.netfauxgo

import _root_.MovableAgent.forward
import _root_.MovableAgent.turn_left
import shapiro.netfauxgo.World

class SpacerAgent(world: World) extends MovableAgent(world) {

  def tick() = {
    if (getOtherAgentsInVicinity(1).length > 1) {
      forward(1)
      turn_left(1)
    }
  }
}