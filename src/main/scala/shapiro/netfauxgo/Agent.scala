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

  var worldSnapshot = new WorldSnapshot(Array[Array[PatchSnapshot]]())

  notifyPatchThatWeHaveStarted(currentPatchRef())

  def doTick(worldSnapshot:WorldSnapshot) = {
    this.worldSnapshot = worldSnapshot
    atomic {
      implicit txn =>
        if (!deadRef.get) tick()
    }
    currentPatchRef() ! TickComplete(snapshot())
    world.manager ! TickComplete
  }

  def tick();

  def killSucceeded(deadGuy:ActorRef, state:Map[_, _]);

  override def receive = {
    case Tick(snapshot) =>
      doTick(snapshot)
    case KillAgent(killer, target) => {
      assert(target == self)
      deadRef.single() = true
      val killSucceeded = KillSucceeded(self, junkRef.single.get)
      currentPatchRef() ! killSucceeded
      killer ! killSucceeded
      self ! PoisonPill
    }
    case KillSucceeded(deadGuy:ActorRef, state:Map[_,_]) => {
      killSucceeded(deadGuy, state)
    }
    case SnapshotRequest => {
      sender ! AgentSnapshotM( snapshot())
    }
  }

  def notifyPatchThatWeHaveStarted(patchRef: ActorRef) = {
    patchRef ! AgentEntered(self)
  }

  def currentPatchRef(): ActorRef = {
    world.patchAt(x.single.get, y.single.get)
  }

  def snapshot():AgentSnapshot = {
    new AgentSnapshot(this.getClass.toString(), x.single.get, y.single.get, junkRef.single.get, self)
  }

  def currentPatch():PatchSnapshot = {
    try{
      worldSnapshot.patches(x.single.get.toInt)(y.single.get.toInt)
    } catch {
      case _ => {
        println("Exception at " + x.single.get + ", " + y.single.get + "patches width = " + worldSnapshot.patches.length + " height = "+ worldSnapshot.patches(0).length)
        currentPatch()
      }
    }
  }

  def getJunk(key:Any): Any = {
    junkRef.single().get(key)
  }

  def setJunk(key:Any, value:Any): Unit = {
    junkRef.single() = junkRef.single() + (key -> value)
  }

  def getOtherAgentsInVicinity(radius: Int): List[AgentSnapshot] = {
    val patches = worldSnapshot.patchSnapshotsWithinRange(x.single(), y.single(), radius)

    val everyone = patches.foldLeft(List[AgentSnapshot]())((l, r) => getAgentsForPatchSnapshot(r) ::: l)
    everyone.filter( a => a != self)
  }

  def getAgentsOnMyPatch() = {
    getAgentsForPatchSnapshot(currentPatch())
  }

  def getAgentsForPatchSnapshot(patchSnapshot:PatchSnapshot) = {
    patchSnapshot.agentSnapshots
  }

  def killAgent(agent:AgentSnapshot):Unit = {
    killAgent(agent.agentRef)
  }

  def killAgent(agentRef: ActorRef):Unit = {
    world.manager ! KillAgent(self, agentRef)
  }

}


