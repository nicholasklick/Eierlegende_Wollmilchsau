import akka.actor._
import akka.actor.Props
import akka.util.duration._
import akka.actor.Scheduler
import akka.actor.ActorSystem


object Driver {
  	def main(arts: Array[String]): Unit = {
	  val system = ActorSystem("DriverSystem")
	  println("Spawning world...")
	  val world = new World(100,100)
	  println("...done")
	  system.scheduler.schedule(50 milliseconds, 100 milliseconds, world.manager, Tick)
	  //OldScheduler.schedule(world.manager, Tick, 0, 50, java.util.concurrent.)
	  println("Scheduler scheduled")
	}
}