package shapiro.netfauxgo

import akka.transactor._
import scala.concurrent.stm._
import akka.actor._

class WorldManager(val world: World) extends Actor {
  private val system = ActorSystem("MySystem")

  private var critters = Vector[ActorRef]()
  //private var critters = Array.tabulate(agentCount){ (i) => system.actorOf(Props(new SpacerAgent(world)))}
  private var agentCount = 0
  //how many agents we ticked in this pass. We need to keep track of this because new guys might get added mid-tick and we'd wait forever for them
  private var agentsComplete = 0

  private var readyForNewTick = true

  var startTime = System.nanoTime()

  def tick() = {
    if (readyForNewTick && critters.length > 0) {
      readyForNewTick = false
      startTime = System.nanoTime()
      agentsComplete = 0
      agentCount = critters.length
      critters.foreach(agent => agent ! Tick)
    }
  }

  def receive = {
    case AddAgent(agent) => {
      critters = critters :+ agent
    }

    case TickComplete => {
      agentsComplete += 1
      if (agentsComplete >= agentCount) {
        val endTime = System.nanoTime()
        val elapsedTime = endTime - startTime
        println("Tick timer: " + elapsedTime / 1E9 + " seconds \t\t" + agentCount + " agents")
        readyForNewTick = true
      }
    }
    case Tick =>
      tick()
  }


}