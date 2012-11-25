package shapiro.netfauxgo

import concurrent.stm.TMap
import akka.actor.ActorPath

trait TickReporter {
  def tickComplete(snapshot:TMap[ActorPath, ActorData]):Unit;
}

class DefaultTickReporter extends TickReporter {
  def tickComplete(snapshot:TMap[ActorPath, ActorData]) = {
  }
}