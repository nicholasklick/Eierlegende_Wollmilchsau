package shapiro.netfauxgo

import concurrent.stm.TMap
import akka.actor.ActorPath

abstract class TickReporter {
  def tickComplete(snapshot:TMap[ActorPath, ActorData]);
}

class DefaultTickReporter extends TickReporter {
  def tickComplete(snapshot:TMap[ActorPath, ActorData]) = {
    //nothing done yet
  }
}