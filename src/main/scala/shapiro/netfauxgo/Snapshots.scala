package shapiro.netfauxgo

import akka.actor.ActorRef
import scala.Array

class WorldSnapshot(val patches:Array[Array[PatchSnapshot]])   {
  val width = patches.length
  val height = if (patches.length > 0) patches(0).length else 0

  def patchSnapshotsWithinRange(x_pos: Double, y_pos: Double, radius: Double): List[PatchSnapshot] = {
    val x_min = if (x_pos - radius >= 0) x_pos - radius else 0
    val x_max = if (x_pos + radius <= width) x_pos + radius else width

    val y_min = if (y_pos - radius >= 0) y_pos - radius else 0
    val y_max = if (y_pos + radius <= height) y_pos + radius else height

    var ret = List[PatchSnapshot]()
    for (x <- x_min.round.until(x_max.round)) {
      for (y <- y_min.round.until(y_max.round)) {
        val thePatch = patches(x.toInt)(y.toInt)
        ret = thePatch :: ret
      }
    }
    ret
  }
}

class PatchSnapshot(val x: Int, val y: Int, val state:Map[Any, Any], val patchRef:ActorRef, val agentSnapshots:List[AgentSnapshot]) {
  def matches( matcher:(Map[Any, Any]) => Boolean ):Boolean = {
    matcher(state)
  }
}


class AgentSnapshot(val klass:String, val x: Double, val y: Double, val state:Map[Any, Any], val agentRef:ActorRef) {
  def matches( matcher:(Map[Any, Any]) => Boolean ):Boolean = {
    matcher(state)
  }

  def matches (klass: String) = {
    klass == this.klass
  }
  def matches( matcher: (Double, Double, (Map[Any, Any])) => Boolean):Boolean = {
    matcher(x, y, state)
  }

  def matches(klass:String)(matcher: (Map[Any, Any]) => Boolean):Boolean = {
    this.klass == klass && matcher(state)
  }

  def matches(klass:String)(matcher: (Double, Double, (Map[Any, Any])) => Boolean):Boolean = {
    this.klass == klass && matcher(x, y, state)
  }
}