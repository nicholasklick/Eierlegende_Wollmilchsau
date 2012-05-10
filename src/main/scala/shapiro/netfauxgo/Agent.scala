package shapiro.netfauxgo

import akka.actor.{ActorSystem, ActorRef, Actor}
import akka.dispatch.{ExecutionContext, Future, Await}
import akka.transactor._
import scala.concurrent.stm._
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import scala.collection.immutable.Map


abstract class Agent(val world: World) extends Actor {
  val x = Ref(world.width * scala.math.random)
  val y = Ref(world.height * scala.math.random)
  implicit val timeout = Timeout(1 seconds) // needed for `?` below

  val junkRef = Ref(Map[Any,Any]())

  val deadRef = Ref(false)

  notifyPatchThatWeHaveStarted(currentPatch())

  def doTick() = {
    //println("In doTick... " + getAgentsForPatchRef(currentPatch).length + " little buddies are here! " + getOtherAgentsInVicinity(2).length + " NEAR here!")
    //println("In doTick... " + getOtherAgentsInVicinity(2).length + " little buddies are NEAR here!")
    atomic {
      implicit txn =>
        if (!deadRef.get) tick()
    }
    world.manager ! TickComplete
    //println("tick complete in doTick")
  }

  def tick();

  override def receive = {
    case Tick =>
      doTick()
    case coordinated @ Coordinated(Die) => die()
  }

  def die() =  atomic {
    implicit txn => if (deadRef.get){
      throw AlreadyDeadException
    } else {
      deadRef.set(true)
    }
  }

  def notifyPatchThatWeHaveStarted(patchRef: ActorRef) = {
    patchRef ! AgentEntered(self)
  }

  def currentPatch(): ActorRef = {
    world.patchAt(x.single(), y.single())
  }

  def getJunk(key:Any): Any = {
    junkRef.single().get(key)
  }

  def setJunk(key:Any, value:Any): Unit = {
    junkRef.single() = junkRef.single() + (key -> value)
  }

  def getOtherAgentsInVicinity(radius: Int): List[ActorRef] = {
    val patches = world.patchesWithinRange(x.single(), y.single(), radius)

    val everyone = patches.foldLeft(List[ActorRef]())((l, r) => getAgentsForPatchRef(r) ::: l)
    everyone.filter( a => a != self)
  }

  def getAgentsOnMyPatch() = {
    getAgentsForPatchRef(currentPatch())
  }

  def getAgentsForPatchRef(patchRef: ActorRef): List[ActorRef] = {
    //	  (patchRef ? FetchAgentRefs).as[AgentsForPatch] match{
    val future = patchRef ? FetchAgentRefs
    val result = Await.result(future, 1 second)
    result match {
      case AgentsForPatch(agentRefs) =>
        agentRefs
      case None =>
        println("Didn't get any agents back, making an empty list")
        List[ActorRef]()
    }
  }

  def killAgent(agentRef: ActorRef) = {
    val didKill = Ref(false)
    val coordinated = Coordinated()
    coordinated atomic { implicit t =>
      didKill.set(true)
      agentRef ! coordinated(Die)
      world.manager ! coordinated(KillAgent(agentRef))
    }
    didKill.single()
  }
}