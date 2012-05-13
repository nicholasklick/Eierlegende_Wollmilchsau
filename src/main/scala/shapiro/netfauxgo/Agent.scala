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

  var snapshot = Array[Array[PatchSnapshot]]()

  notifyPatchThatWeHaveStarted(currentPatch())



  def doTick(snapshot:Array[Array[PatchSnapshot]]) = {
    this.snapshot = snapshot
    atomic {
      implicit txn =>
        if (!deadRef.get) tick()
    }
    world.manager ! TickComplete
  }

  def tick();

  def killSucceeded(deadGuy:ActorRef, state:Map[Any, Any]);

  override def receive = {
    case Tick(snapshot) =>
      doTick(snapshot)
    case KillAgent(killer, target) => {
      assert(target == self)
      deadRef.single() = true
      currentPatch() ! AgentLeft(self)
      killer ! KillSucceeded(self, junkRef.single.get)
      self ! PoisonPill
    }
    case KillSucceeded(deadGuy:ActorRef, state:Map[Any,Any]) => {
      killSucceeded(deadGuy, state)
    }
    case SnapshotRequest => {
      sender ! AgentSnapshotM( new AgentSnapshot(this.getClass.toString(), x.single.get, y.single.get, junkRef.single.get, self))
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

  def killAgent(agentRef: ActorRef) = {
    world.manager ! KillAgent(self, agentRef)
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