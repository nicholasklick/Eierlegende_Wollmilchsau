import scala.concurrent.stm._

abstract class MovableAgent(world:World) extends Agent(world) {

	val heading = Ref(360 * scala.math.random)
	
	def forward(steps:Double) = {
	  atomic { 
	    implicit txn => {
	      val oldX = x.get
		  val oldY = y.get
		  var newX = (oldX + steps * scala.math.cos(scala.math.Pi*(heading.get/180.0))) % world.width
		  var newY = (oldY + steps * scala.math.sin(scala.math.Pi*(heading.get/180.0))) % world.height
		  
		  if (newY < 0)
		    newY += world.height
		  if (newX < 0)
		    newX += world.width
		  
		  x.swap(newX)
		  y.swap(newY)
		  
	
		  val oldPatch = currentPatch
	  	  currentPatch = findCurrentPatch()
		  val newPatch = currentPatch
		  
		  if (oldPatch != newPatch){
			  world.agentPatchMover ! MovePatches(self, oldPatch, newPatch)
		  }
	    }
	  }
	}
	
	def turn_right(degrees:Double) = {
	  atomic{
	    implicit txn => heading.swap((heading.get - degrees) % 360  )
	  }
	}
	
	def turn_left(degrees:Double) = {
	  turn_right(0 - degrees)
	}
}