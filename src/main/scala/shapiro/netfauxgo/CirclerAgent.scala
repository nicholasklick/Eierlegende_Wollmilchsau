package shapiro.netfauxgo

import akka.actor.ActorRef

class CirclerAgent(world: World) extends MovableAgent(world) {

  def tick() = {
    forward(1)
    turn_right(1)
  }

  def killSucceeded(deadGuy:ActorRef, state:Map[_, _]):Unit ={
    println("I pity the foo! " + deadGuy)
  }
}