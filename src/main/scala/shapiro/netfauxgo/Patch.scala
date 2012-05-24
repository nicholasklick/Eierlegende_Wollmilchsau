package shapiro.netfauxgo

import akka.transactor._
import scala.concurrent.stm._
import akka.actor._
import scala.collection.immutable.Vector
import scala.collection.immutable.Map
import akka.dispatch.Await
import akka.util.duration._
import akka.pattern.ask
import akka.util.Timeout



class Patch(val world: World, val x: Int, val y: Int) extends Transactor {
  val agentRefs = Ref(Vector[ActorRef]())
  val junkRef = Ref(Map[Any,Any]())

  val currentSnapshottedAgents = Ref(Map[ActorRef, AgentSnapshot]())

  def getJunk(key:Any): Any = {
    junkRef.single().get(key).get
  }

  def setJunk(key:Any, value:Any): Unit = {
    junkRef.single() = junkRef.single() + (key -> value)
  }

  def agentEntered(agentRef: ActorRef) {
    atomic {
      implicit txn => agentRefs.set(agentRefs.get :+ agentRef)
    }
    //println("Agent " + agentRef + " entered patch ("+this+")")
  }

  def agentLeft(agentRef: ActorRef) {
    atomic {
      implicit txn => {
        agentRefs.set(agentRefs.get.filter((ar) => ar != agentRef))
        currentSnapshottedAgents.transform { _ - agentRef }
      }
    }
    //println("Agent " + agentRef + " left patch ("+this+")")
  }

  override def atomically = {
    implicit txn => {
      case AgentLeft(agentRef) =>
        agentLeft(agentRef)
      case AgentEntered(agentRef) =>
        agentEntered(agentRef)
    }
  }

  override def normally = {
    case FetchAgentRefs =>
      atomic {
        implicit txn => {
          sender ! AgentsForPatch(agentRefs.get.toList)
        }
      }
    case SnapshotRequest => {
       sender ! PatchSnapshotM(getSnapshot())
    }
    case TickComplete(agentSnapshot) => {
      atomic {
        implicit txn => {
          currentSnapshottedAgents.transform { _ + (agentSnapshot.agentRef -> agentSnapshot)  }
        }
      }
    }
    case TickAgents(snapshot) => {
      currentSnapshottedAgents.single() = Map[ActorRef, AgentSnapshot]()
      agentRefs.single.get.foreach(agent => agent ! Tick(snapshot))
    }

  }

  def getSnapshot():PatchSnapshot = {
    new PatchSnapshot(x, y, junkRef.single.get, self, getAgentSnapshots().toList)
  }

  def getAgentSnapshots():Iterable[AgentSnapshot] = {
    currentSnapshottedAgents.single.get.values
  }
}

