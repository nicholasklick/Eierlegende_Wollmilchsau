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
  val data = new ActorData(self, getClass.toString)
  data.setProperty("agentRefs", Ref(Vector[ActorRef]()))
  data.setPosition(x, y)
  world.registerActorData(self.path, data)


  def agentRefs = {
    data.getProperty("agentRefs").asInstanceOf[Ref[Vector[ActorRef]]]
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
  }

}

