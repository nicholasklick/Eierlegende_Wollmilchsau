package shapiro.netfauxgo

import scala.concurrent.stm._

abstract class MovableAgent(world: World) extends Agent(world) {

  setProperty("heading", 360 * scala.math.random)

  def forward(steps: Double) = {
    atomic {
      implicit txn => {
        val oldPatch = currentPatch()
        val oldPosition = data.getPosition()
        val oldX = oldPosition._1
        val oldY = oldPosition._2
        val heading = data.getProperty("heading").asInstanceOf[Double]
        var newX = (oldX + steps * scala.math.cos(scala.math.Pi * (heading / 180.0))) % world.width
        var newY = (oldY + steps * scala.math.sin(scala.math.Pi * (heading / 180.0))) % world.height

        while (newY < 0)
          newY += world.height
        while (newX < 0)
          newX += world.width

        data.setPosition(newX, newY)

        val newPatch = currentPatch()

        if (oldPatch != newPatch) {
          world.agentPatchMover ! MovePatches(self, oldPatch, newPatch)
        }
      }
    }
  }

  def turn_right(degrees: Double) = {
    atomic {
      implicit txn =>
        val oldHeading = getProperty("heading").asInstanceOf[Double]
        setProperty("heading", (oldHeading - degrees) % 360)
    }
  }

  def turn_left(degrees: Double) = {
    turn_right(0 - degrees)
  }
}