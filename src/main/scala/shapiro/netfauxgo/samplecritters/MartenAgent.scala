package shapiro.netfauxgo.samplecritters

import shapiro.netfauxgo.{World, MovableAgent}
import shapiro.netfauxgo.AddAgent
import akka.actor.Props


class MartenAgent(world: World) extends MovableAgent(world) {
  def tick() = {
    eatSomeVoles
	  goToABetterPlace
	  leaveScent
	  if (!dieMaybe) reproduce
  }

  def eatSomeVoles = {
    val patch = currentPatch()
    val currentVolePopulation = getActorProperty(patch, "vole_population").asInstanceOf[Double]
    //eat and keep the energy should be here
    val volesLeft = currentVolePopulation * 0.75
    setActorProperty(patch, "vole_population", volesLeft)
  }

  def goToABetterPlace = {
	//FIXME: right now, it's just a random place
    turn_right(-90 + 180 * scala.math.random)
    forward(1)
  }

  def leaveScent = {
    val patch = currentPatch()
	  setActorProperty(patch, "smelliest_marten", self)
	  setActorProperty(patch, "marten_scent_age", 0)
  }

  def dieMaybe = {
	if (scala.math.random > 0.98) {
      die
      true
	} else false
  }

  def reproduce = {
	if (scala.math.random > 0.98) {
      val littleDude = world.system.actorOf(Props(new MartenAgent(world))) // TODO: make spawning much cleaner
      world.manager ! AddAgent(littleDude)
      true	
    } else false
  }

}

