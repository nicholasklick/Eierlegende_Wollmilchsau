package shapiro.netfauxgo

import akka.actor._
import concurrent.stm.TSet


class World(val width: Int, val height: Int) {
  private val system = ActorSystem("MySystem")
  private val grid = Array.tabulate(width, height) {
    (x, y) => system.actorOf(Props(new Patch(this, x, y)))
  }
  val manager = system.actorOf(Props(new WorldManager(this)))
  val agentPatchMover = system.actorOf(Props[AgentPatchMover])
  //Actor.actorOf[AgentPatchMover].start()

  def patchAt(x: Double, y: Double): ActorRef = {
    grid(x.toInt)(y.toInt)
  }

  def patchesWithinRange(x_pos: Double, y_pos: Double, radius: Double): List[ActorRef] = {
    val x_min = if (x_pos - radius >= 0) x_pos - radius else 0
    val x_max = if (x_pos + radius <= width) x_pos + radius else width

    val y_min = if (y_pos - radius >= 0) y_pos - radius else 0
    val y_max = if (y_pos + radius <= height) y_pos + radius else height

    var ret = List[ActorRef]()
    for (x <- x_min.round.until(x_max.round)) {
      for (y <- y_min.round.until(y_max.round)) {
        val thePatch = grid(x.toInt)(y.toInt)
        ret = thePatch :: ret
      }
    }
    ret
  }

}

class PatchSnapshot(val world:World, val x: Int, val y: Int, val state:Map[Any, Any])  {
  val agents = TSet[Map[Any, Any]]()
}

class AgentSnapshot(val world:World, val klass:String, val x: Double, val y: Double, val state:Map[Any, Any]) {
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