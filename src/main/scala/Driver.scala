import akka.actor._
import akka.actor.Props
import akka.dispatch.DefaultDispatcherPrerequisites
import akka.util.duration._
import akka.actor.Scheduler
import akka.actor.ActorSystem
import shapiro.netfauxgo._

class Driver(val width:Int, val height:Int, val patchSpawner:PatchSpawner) {
  val system = ActorSystem("DriverSystem")

  print("Spawning world...")
  val world = new World(width, height, patchSpawner)
  println("...done")

  def startTicking():Unit = {
    system.scheduler.schedule(50 milliseconds, 100 milliseconds, world.manager, Tick)
    println("Ticking!")
  }

}

object DefaultDriver {
  val driver = new Driver(100, 100, new DefaultPatchSpawner())

  def main(args: Array[String]): Unit = {
    print("Spawning starter agents...")
    for (i <- 0 until driver.world.width * driver.world.height / 2) {
      val dude = driver.system.actorOf(Props(new MurderousSpacerAgent(driver.world)).withDispatcher("kill-prioritizer"))
      driver.world.manager ! AddAgent(dude)
    }
    println("...done")

    driver.startTicking()
  }
}