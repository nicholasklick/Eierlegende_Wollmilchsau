import akka.actor._
import akka.actor.Props
import akka.util.duration._
import akka.actor.Scheduler
import akka.actor.ActorSystem
import shapiro.netfauxgo._

object Driver {
  val system = ActorSystem("DriverSystem")

  print("Spawning world...")
  val world = new World(100, 100)
  println("...done")


  def startTicking():Unit ={
	system.scheduler.schedule(50 milliseconds, 100 milliseconds, world.manager, Tick)
	println("Ticking!")
  }

  def main(arts: Array[String]): Unit = {
    // for (i <- 0 until world.width * world.height / 4) {
    //   val dude = system.actorOf(Props(new MurderousSpacerAgent(world)).withDispatcher("kill-prioritizer"))
    //   world.manager ! AddAgent(dude)
    // }
	print("Spawning starter agents...")
    for (i <- 0 until world.width * world.height / 2) {
      val dude = system.actorOf(Props(new MurderousSpacerAgent(world)).withDispatcher("kill-prioritizer"))
      world.manager ! AddAgent(dude)
    }
	println("...done")
	startTicking()
  }

}
