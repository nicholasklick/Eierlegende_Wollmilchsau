import akka.transactor._
import scala.concurrent.stm._
import akka.actor._

class WorldManager(val world:World) extends Actor {
	private val system = ActorSystem("MySystem")

  	private val agentCount = world.width * world.height / 2
	private val critters = Array.tabulate(agentCount){ (i) => system.actorOf(Props(new SpacerAgent(world)))}
  	private val agentsComplete = Ref(0)
  	
  	val readyForNewTick = Ref(true)

	println("World manager ready!")

  	var startTime = System.nanoTime()
  	  	
	def tick() = {
		atomic { 
		  implicit txn =>{
			//println("in tick atomic section")
			if (readyForNewTick.get) {
				readyForNewTick.set(false)
		  	    startTime = System.nanoTime()
		  	    agentsComplete.set(0)
		  	    critters.foreach( agent => agent ! Tick )
			}
		  }
		}
	}
  	
  	def receive = {
  	  case TickComplete => 
  	    atomic{ 
  	      implicit txn => {
  	        val complete = agentsComplete.get + 1
  	    	agentsComplete.set(complete)
  	    	if (complete >= agentCount ){
	    	  val endTime = System.nanoTime()
	    	  val elapsedTime = endTime - startTime
	    	  println("Tick timer: " + elapsedTime / 1000000000.0)
	    	  readyForNewTick.set(true)
  	    	}
  	      }
  	    }
  	  case Tick =>
  	    tick()
  	}
	
	
}