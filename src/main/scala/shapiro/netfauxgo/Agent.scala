package shapiro.netfauxgo

import akka.dispatch.{ExecutionContext, Future, Await}
import scala.concurrent.stm._
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import scala.collection.immutable.Map
import akka.actor.{PoisonPill, ActorSystem, ActorRef, Actor}
import java.util.NoSuchElementException

abstract class Agent(val world: World) extends Actor {
  val data = new ActorData(self, getClass.toString)
  data.setPosition(scala.math.random * world.width, scala.math.random * world.height)
  world.registerActorData(self.path, data)

  implicit val timeout = Timeout(1 seconds) // needed for `?` below

  notifyPatchThatWeHaveStarted(currentPatch())

  def doTick() = {
    atomic {
      implicit txn =>
        if (!data.isDead()){
          tick()
          world.manager ! TickComplete(self)
        }
    }
  }

  def tick();

  def die() = {
    data.die()
    world.manager ! AgentDied(self)
    ///// TODO /////
    /// add code to remove from database here ///
    /// and maybe some after death callbacks? ///
    //// TODO /////
    self ! PoisonPill
  }

  override def receive = {
    case Tick =>
      doTick()
//    case KillAgent(killer, target) => {
//      assert(target == self)
//      deadRef.single() = true
//      val killSucceeded = KillSucceeded(self, junkRef.single.get)
//      currentPatchRef() ! killSucceeded
//      killer ! killSucceeded
//      self ! PoisonPill
//    }
    case Die =>
      die()
  }

  def notifyPatchThatWeHaveStarted(patchRef: ActorRef) = {
    patchRef ! AgentEntered(self)
  }

  def currentPatch():ActorRef = {
    val position = data.getPosition
    world.patchAt(position._1, position._2)
  }

  def getProperty(key:String): Any = {
    data.getProperty(key)
  }

  def setProperty(key:String, value:Any) = {
    data.setProperty(key, value)
  }

  def getActorProperty(otherActor:ActorRef, property:String) = {
    world.getActorData(otherActor.path).getProperty(property)
  }

  def setActorProperty(otherActor:ActorRef, property:String, value:Any) = {
    world.getActorData(otherActor.path).setProperty(property, value)
  }

  def getActorPosition(otherActor:ActorRef) = {
    world.getActorData(otherActor.path).getPosition()
  }

  def setActorPosition(otherActor:ActorRef, x:Double, y:Double) = {
    world.getActorData(otherActor.path).setPosition(x, y)
  }

  def getActorClass(otherActor:ActorRef) = {
    world.getActorData(otherActor.path).klass
  }

  def getOtherAgentsInVicinity(radius: Int): List[ActorRef] = {
    val position = data.getPosition()
    //val patches = worldSnapshot.patchSnapshotsWithinRange(x.single(), y.single(), radius)
    val patches = world.patchRefsWithinRange(position._1, position._2, radius)
    val everyone = patches.foldLeft(List[ActorRef]())((l, r) => getAgentsForPatchRef(r) ::: l)
    everyone.filter( a => a != self)
  }

  def getAgentsOnMyPatch() = {
    getAgentsForPatchRef(currentPatch())
  }

  def getAgentsForPatchRef(patchRef:ActorRef) = {
    world.getActorData(patchRef.path).getProperty("agentRefs").asInstanceOf[Ref[Vector[ActorRef]]].single.get.toList
  }

  def killAgent(otherAgent: ActorRef):Boolean = atomic { implicit txn =>
    try {
      val otherAgentData = world.getActorData(otherAgent.path)
      if (!otherAgentData.isDead) {
        otherAgentData.die
        otherAgent ! Die
        true
      }else{
        false
      }
    } catch {
      case ex:NoSuchElementException => false
    }
  }

}


