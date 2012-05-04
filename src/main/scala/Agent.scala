import akka.actor.{ActorSystem, ActorRef}
import akka.dispatch.{ExecutionContext, Future, Await}
import akka.transactor._
import scala.concurrent.stm._
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._


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
    //println("In doTick... " + otherAgentsInVicinity(2).length + " little buddies are NEAR here!")
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
	
	def getOtherAgentsInVicinity(radius:Int):List[ActorRef] = {
    //implicit val context = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
    //implicit val actorSystem = ActorSystem("MySystem")
    implicit val actorSystem = context.system
    implicit def agentsForPatch2ListOfActorRefs(afp:AgentsForPatch): List[ActorRef] = afp.agentRefs
	  atomic{
      implicit txn => {
        val patches = world.patchesWithinRange(x(), y(), radius)
        val agentRefsFutures = patches.map( (patchRef) => patchRef ? FetchAgentRefs )
        val futureFold = Future.fold(agentRefsFutures)(List[ActorRef]())(_.asInstanceOf[List[ActorRef]] ::: _.asInstanceOf[AgentsForPatch])
        Await.result(futureFold.asInstanceOf[akka.dispatch.Await.Awaitable[List[ActorRef]]], 1 seconds)
      }
    }
	}
	 
	def getAgentsOnMyPatch() = {
	  getAgentsForPatchRef(currentPatch)
	}
	
	def getAgentsForPatchRef(patchRef:ActorRef):List[ActorRef] = {
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


}