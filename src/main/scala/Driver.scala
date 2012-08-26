import akka.actor._
import akka.actor.Props
import akka.dispatch.DefaultDispatcherPrerequisites
import akka.util.duration._
import akka.actor.Scheduler
import akka.actor.ActorSystem
import shapiro.netfauxgo._
import shapiro.netfauxgo.samplecritters.{VolePatchSpawner, MartenAgent}

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

object VoleDriver {
  val driver = new Driver(1353, 714, new VolePatchSpawner())
  //driver.world.manager ! RegisterTickReporter(new DefaultTickReporter())

  def main(args: Array[String]): Unit = {
    print("Spawning starter agents...")
    //for (i <- 0 until driver.world.width * driver.world.height / 2) {

	for (i <- 0 until 200) {
	  val dude = driver.system.actorOf(Props(new MartenAgent(driver.world)))
	  driver.world.manager ! AddAgent(dude)
	}
    println("...done")

    driver.startTicking()
  }
}