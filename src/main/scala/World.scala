import akka.actor._


class World(val width:Int, val height:Int)  {
	private val system = ActorSystem("MySystem")
	private val grid = Array.tabulate(width, height){ (x,y) => system.actorOf(Props(new Patch(this, x, y)))}
	val manager = system.actorOf( Props( new WorldManager(this) ) )
  	val agentPatchMover = system.actorOf(Props[AgentPatchMover])
	//Actor.actorOf[AgentPatchMover].start()

	def patchAt(x:Double, y:Double):ActorRef = {
	  return grid(x.toInt)(y.toInt) 
	}
	
}