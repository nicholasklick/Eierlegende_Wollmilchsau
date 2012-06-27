package shapiro.netfauxgo

import scala.concurrent.stm._
import akka.actor._
import akka.dispatch._
import akka.util.duration._
import akka.pattern.ask
import akka.util.Timeout



class WorldManager(val world: World) extends Actor {
  private val system = ActorSystem("MySystem")

  private var critters = Set[ActorRef]()
  private var outstandingCritters = Set[ActorRef]()
  private var agentCount = 0       //how many agents we ticked in this pass.

  private var tickNumber = 1

  private var readyForNewTick = true

  private var deadPool = makeNewDeadpool()  // these guys have been killed this tick, and should be removed from the database and the list of critters to tick

  var startTime = System.nanoTime()

  def tick() = {
    if (readyForNewTick && critters.size > 0) {
      println("Beginning tick #" + tickNumber)
      tickNumber += 1
      val tick = Tick
      readyForNewTick = false
      startTime = System.nanoTime()
      //agentsComplete = 0
      agentCount = critters.size
      outstandingCritters = critters
      critters.foreach(agent => agent ! tick)
    }
  }

  def tellAgentToDie(target:ActorRef, message:KillAgent) = {
    target ! message
  }

  def receive = {
    case AddAgent(agent) => {
      critters += agent
    }

    case TickComplete(agent) => {
      outstandingCritters -= agent
      //println("An agent FINISHED and now there are " + outstandingCritters.size + " outstanding")
      if (outstandingCritters.size == 0) {
        finishTick
      }
    }
    case Tick =>
      tick()

    case AgentDied(agent) => {
      outstandingCritters -= agent
      critters -= agent
      world.unregisterActorData(agent.path)
      //println("An agent DIED and now there are " + outstandingCritters.size + " outstanding")
      if (outstandingCritters.size == 0) {
        finishTick
      }
    }
  }
  def makeNewDeadpool():TMap[ActorRef, KillAgent] = {
    TMap[ActorRef, KillAgent]()
  }


  def finishTick() = {
    val endTime = System.nanoTime()
    val elapsedTime = endTime - startTime
    println("\t" + elapsedTime / 1E9 + " seconds \t\t" + agentCount + " agents")
    readyForNewTick = true
  }

}



