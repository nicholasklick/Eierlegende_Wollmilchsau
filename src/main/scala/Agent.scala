import akka.transactor._
import scala.concurrent.stm._
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import akka.dispatch.Await

abstract class Agent(val world:World) extends Transactor {
	val x = Ref(world.width * scala.math.random)
	val y = Ref(world.height * scala.math.random)	
	implicit val timeout = Timeout(1 seconds) // needed for `?` below

	
	var currentPatch = findCurrentPatch()
	
	atomic { implicit txn =>
	  notifyPatchThatWeHaveStarted(currentPatch)
	}
	
	def doTick() = {
	  //println("In doTick... " + getAgentsForPatchRef(currentPatch).length + " little buddies are here!")
	  atomic { implicit txn =>
	    tick()
	  }
	  world.manager ! TickComplete
	  //println("tick complete in doTick")
	}
	
	def tick();
	
	override def atomically = implicit txn => {
	  case Tick =>
	    doTick()
	}
	
	def notifyPatchThatWeHaveStarted(patchRef:ActorRef) = {
	  patchRef ! AgentEntered(self)
	}
	
	def findCurrentPatch():ActorRef = {
	  atomic {
	    implicit txn => world.patchAt(x.get, y.get)
	  }
	}
	
//	def otherAgentsInVicinity(radius:Int):List[ActorRef] = {
//	  atomic{
//		  val patches = world.patchesWithinRange(x.get(), x.get(y), radius)
//		  val agents = patches.map(p.  )
//	}
//	 
	def getAgentsOnMyPatch() = {
	  getAgentsForPatchRef(currentPatch)
	}
	def getAgentsForPatchRef(patchRef:ActorRef):scala.collection.immutable.Vector[ActorRef] = {
//	  (patchRef ? FetchAgentRefs).as[AgentsForPatch] match{
	  val future = patchRef ? FetchAgentRefs
	  val result = Await.result(future, 1 second)
	  result match {
	    case AgentsForPatch(agentRefs) =>
	      agentRefs
	    case None => 
	      println("Didn't get any agents back, making an empty list")
	      scala.collection.immutable.Vector[ActorRef]()
	  }
	}
}