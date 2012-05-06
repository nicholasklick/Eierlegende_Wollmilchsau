import akka.actor._
import akka.actor.Props
import akka.util.duration._
import akka.actor.Scheduler
import akka.actor.ActorSystem
import shapiro.netfauxgo._

object Driver {
  val system = ActorSystem("DriverSystem")

  print("Spawning world...")
  val world = new World(1000, 1000)
  println("...done")
  system.scheduler.schedule(50 milliseconds, 100 milliseconds, world.manager, Tick)


  def main(arts: Array[String]): Unit = {
    for (i <- 0 until world.width * world.height / 2)
      world.manager ! AddAgent(system.actorOf(Props(new SpacerAgent(world))))
  }

}
