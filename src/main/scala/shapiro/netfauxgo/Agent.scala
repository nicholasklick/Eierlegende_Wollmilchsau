package shapiro.netfauxgo

import akka.dispatch.{ExecutionContext, Future, Await}
import scala.concurrent.stm._
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import scala.collection.immutable.Map
import akka.actor.{PoisonPill, ActorSystem, ActorRef, Actor}

abstract class Agent(val world: World) extends Actor {
  val x = Ref(world.width * scala.math.random)
  val y = Ref(world.height * scala.math.random)
  implicit val timeout = Timeout(1 seconds) // needed for `?` below

  val junkRef = Ref(Map[Any,Any]())

  val deadRef = Ref(false)

  notifyPatchThatWeHaveStarted(currentPatch())

  def doTick() = {
    atomic {
      implicit txn =>
        if (!deadRef.get) tick()
    }
    world.manager ! TickComplete
  }

  def tick();

  def killSucceeded(deadGuy:ActorRef, state:Map[Any, Any]);

  override def receive = {
    case Tick =>
      doTick()
    case KillAgent(killer, target) => {
      assert(target == self)
      deadRef.single() = true
      killer ! KillSucceeded(self, junkRef.single.get)
      self ! PoisonPill
    }
    case KillSucceeded(deadGuy:ActorRef, state:Map[Any,Any]) => {
      killSucceeded(deadGuy, state)
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
      case None => {
        println("Didn't get any agents back, making an empty list")
        List[ActorRef]()
      }
    }
  }

  def killAgent(agentRef: ActorRef) = {
    world.manager ! KillAgent(self, agentRef)
  }
}