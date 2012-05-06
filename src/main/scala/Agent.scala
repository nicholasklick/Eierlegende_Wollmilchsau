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
    //println("In doTick... " + getAgentsForPatchRef(currentPatch).length + " little buddies are here! " + getOtherAgentsInVicinity(2).length + " NEAR here!")
    //println("In doTick... " + getOtherAgentsInVicinity(2).length + " little buddies are NEAR here!")
    atomic { implicit txn =>
	    tick()
	  }
	  world.manager ! TickComplete
	  //println("tick complete in doTick")
	}
	
	def tick();
	
	override def atomically = implicit txn => {
    case message =>
  }

  override def normally =  {
	  case Tick =>
	    doTick()
	}
	
	def notifyPatchThatWeHaveStarted(patchRef:ActorRef) = {
	  patchRef ! AgentEntered(self)
	}
	
	def findCurrentPatch():ActorRef = {
	  world.patchAt(x.single(), y.single())
	}

  // This doesn't work, even though it should. Everything just starts timing out... not sure why. I'm guessing the Await.result blocking doesn't release control of the thread
	/*
  def getOtherAgentsInVicinityParallel(radius:Int):List[ActorRef] = {
    //implicit val context = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
    //implicit val actorSystem = ActorSystem("MySystem")
    implicit val actorSystem = context.system
    implicit def agentsForPatch2ListOfActorRefs(afp:AgentsForPatch): List[ActorRef] = afp.agentRefs

    val patches = world.patchesWithinRange(x.single(), y.single(), radius)
    val agentRefsFutures = patches.map( (patchRef) => patchRef ? FetchAgentRefs )
    val futureFold = Future.fold(agentRefsFutures)(List[ActorRef]())(_.asInstanceOf[List[ActorRef]] ::: _.asInstanceOf[AgentsForPatch])
    Await.result(futureFold.asInstanceOf[akka.dispatch.Await.Awaitable[List[ActorRef]]], 1 seconds)
	}
	*/

  def getOtherAgentsInVicinity(radius:Int):List[ActorRef] = {
    val patches = world.patchesWithinRange(x.single(), y.single(), radius)

    patches.foldLeft(List[ActorRef]())( (l, r) => getAgentsForPatchRef(r) ::: l )
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