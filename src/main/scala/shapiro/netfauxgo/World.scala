package shapiro.netfauxgo

import akka.actor._
import concurrent.stm._
import akka.dispatch.Await
import scala.math.{max, min}


class World(val width: Int, val height: Int, val patchSpawner:PatchSpawner) {
  val system = ActorSystem("MySystem")
  val bubbler = system.actorOf(Props(new Bubbler(this)))
  private val actorData = TMap.empty[ActorPath, ActorData]

  private val grid = Array.tabulate(width, height) {
    //new Patch(this, x, y)
    (x, y) => system.actorOf(Props(patchSpawner.spawnPatch(this, x, y)))
  }

  val manager = system.actorOf(Props(new WorldManager(this) ))

  val agentPatchMover = system.actorOf(Props[AgentPatchMover])

  def patchAt(x: Double, y: Double): ActorRef = {
	val xToUse = (x + width) % width
	val yToUse = (y + height) % height
    grid(xToUse.toInt)(yToUse.toInt)
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
	// val xOne = (x_pos + radius + width) % width
	// val xAnother = (x_pos - radius + width) % width
	// 
	// val yOne = (y_pos + radius + height) % height
	// val yAnother = (y_pos - radius + height) % height
	// 
	//     val x_min = min(xOne, xAnother)
	//     val x_max = if (x_pos + radius < width) x_pos + radius else width - 1
	// 
	//     val y_min = if (y_pos - radius >= 0) y_pos - radius else 0
	//     val y_max = if (y_pos + radius < height) y_pos + radius else height - 1

    //println("Looking for patches in range: x[" + x_min + " .. " + x_max + "] y[" + y_min + " .. " + y_max + "]")

    var ret = List[ActorRef]()
    for (x <- (x_pos - radius).toInt.until((x_pos + radius).toInt + 1)) {
      for (y <- (y_pos - radius).toInt.until((y_pos + radius).toInt + 1)) {
        val thePatch = patchAt(x, y)
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

