package shapiro.netfauxgo

import akka.transactor._
import scala.concurrent.stm._
import akka.actor._

class WorldManager(val world: World) extends Actor {
  private val system = ActorSystem("MySystem")

  private var critters = Vector[ActorRef]()
  private var agentCount = 0
  //how many agents we ticked in this pass. We need to keep track of this because new guys might get added mid-tick and we'd wait forever for them
  private var agentsComplete = 0

  private var readyForNewTick = true

  private var deadPool = Ref(Vector[ActorRef]()) // these guys have been killed this tick, and should be removed from the database and the list of critters to tick

  var startTime = System.nanoTime()

  def tick() = {
    val pool = deadPool.single()
    if (pool.length > 0){
      pool.foreach( c => c ! PoisonPill)
      critters = critters.filter(c => !pool.contains(c))
      ///// FIXME /////
      /// add code to remove from database here ///
      //// FIXME /////
      deadPool.single()=Vector[ActorRef]()
    }
    if (readyForNewTick && critters.length > 0) {
      readyForNewTick = false
      startTime = System.nanoTime()
      agentsComplete = 0
      agentCount = critters.length
      critters.foreach(agent => agent ! Tick)
    }
  }

  def receive = {
    case AddAgent(agent) => {
      critters = critters :+ agent
    }

    case TickComplete => {
      agentsComplete += 1
      if (agentsComplete >= agentCount) {
        val endTime = System.nanoTime()
        val elapsedTime = endTime - startTime
        println("Tick timer: " + elapsedTime / 1E9 + " seconds \t\t" + agentCount + " agents")
        readyForNewTick = true
      }
    }
    case Tick =>
      tick()

    case coordinated @ Coordinated(KillAgent(agentRef)) => {
      coordinated atomic  { implicit t => deadPool.set(deadPool.get :+ agentRef) }
    }
  }


}