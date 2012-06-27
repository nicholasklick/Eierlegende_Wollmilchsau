package shapiro.netfauxgo

import akka.actor.ActorRef

class SpacerAgent(world: World) extends MovableAgent(world) {

  def tick() = {
    val otherGuys = getOtherAgentsInVicinity(1)
    if (otherGuys.length > 1) {
      wiggle
    }
  }

  def wiggle() = {
    forward(5 * scala.math.random)
    turn_right(-10 + 20 * scala.math.random)
  }

  def killSucceeded(deadGuy:ActorRef, state:Map[_, _]):Unit ={
    //println("I pity the foo! " + deadGuy)
  }
}
