package shapiro.netfauxgo

import akka.actor._
import concurrent.stm._
import akka.dispatch.Await


class World(val width: Int, val height: Int, val patchSpawner:PatchSpawner) {
  val system = ActorSystem("MySystem")
  private val actorData = TMap.empty[ActorPath, ActorData]

  private val grid = Array.tabulate(width, height) {
    //new Patch(this, x, y)
    (x, y) => system.actorOf(Props(patchSpawner.spawnPatch(this, x, y)))
  }

  val manager = system.actorOf(Props(new WorldManager(this) ))

  val agentPatchMover = system.actorOf(Props[AgentPatchMover])

  def patchAt(x: Double, y: Double): ActorRef = {
    grid(x.toInt)(y.toInt)
  }

  def col(x:Int) = {
    grid(x)
  }

  def registerActorData(uuid: ActorPath, ad:ActorData) = {
    actorData.single += (uuid -> ad)
  }

  def getActorData(uuid: ActorPath) = {
    actorData.single(uuid)
  }

  def unregisterActorData(uuid: ActorPath) = {
    actorData.single -= uuid
  }

  def patchRefsWithinRange(x_pos: Double, y_pos: Double, radius: Double): List[ActorRef] = {
    val x_min = if (x_pos - radius >= 0) x_pos - radius else 0
    val x_max = if (x_pos + radius < width) x_pos + radius else width - 1

    val y_min = if (y_pos - radius >= 0) y_pos - radius else 0
    val y_max = if (y_pos + radius < height) y_pos + radius else height - 1

    //println("Looking for patches in range: x[" + x_min + " .. " + x_max + "] y[" + y_min + " .. " + y_max + "]")

    var ret = List[ActorRef]()
    for (x <- x_min.toInt.until(x_max.toInt + 1)) {
      for (y <- y_min.toInt.until(y_max.toInt + 1)) {
        val thePatch = grid(x)(y)
        ret = thePatch :: ret
      }
    }
    ret
  }

  def getActorDataSnapshot() = {
    atomic {
      implicit txn =>
        val snapshot = TMap.empty[ActorPath, ActorData]
        actorData.foreach( (t2) => snapshot.put(t2._1, t2._2.clone))
        snapshot
    }
  }
}

