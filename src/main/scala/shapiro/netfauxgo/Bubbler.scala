package shapiro.netfauxgo

import java.awt.image.BufferedImage
import scala.swing._
import akka.actor._
import scala.swing._
import javax.swing.Timer
import java.awt.event.{ActionEvent, ActionListener}
import java.awt.{AlphaComposite, Graphics2D}
//import swing.Graphics2D

class Bubbler(world:World) extends Actor{
  val drawingPanel = new AgentPanel(world)
  val frame = new Frame {
    title = "EggPig"
    contents = drawingPanel
  }

  frame.visible = true

  override def receive = {
    case DrawPatch(data) => {
      drawingPanel.drawPatch(data)
    }
    case DrawAgent(data) => {
      drawingPanel.drawAgent(data)
    }
    case TickComplete => {
      drawingPanel.tickComplete
    }
  }
}

class AgentPanel(world:World) extends Panel {
  preferredSize = new Dimension(world.width, world.height)

  val patchBuffer = new BufferedImage(world.width, world.height, BufferedImage.TYPE_INT_ARGB)
  patchBuffer.getGraphics.drawLine(0,0,world.width - 1, world.height - 1)

  val agentBuffer = new BufferedImage(world.width, world.height, BufferedImage.TYPE_INT_ARGB)
  agentBuffer.getGraphics.drawLine(world.width - 1, 0, 0, world.height - 1)

  val combinedBuffer =  new BufferedImage(world.width, world.height, BufferedImage.TYPE_INT_ARGB)

  def tickComplete = {
    val combinedBufferGraphics = combinedBuffer.getGraphics()

    patchBuffer.synchronized {
      agentBuffer.synchronized {
        combinedBufferGraphics.drawImage(patchBuffer, 0, 0, null)
        combinedBufferGraphics.drawImage(agentBuffer, 0, 0, null)

        val agentBufferGraphics:java.awt.Graphics2D = agentBuffer.getGraphics().asInstanceOf[Graphics2D]
        agentBufferGraphics.setBackground(new Color(255,255,255,0))
        agentBufferGraphics.clearRect(0, 0, world.width, world.height)
      }
    }
    repaint()

  }

  override def paintComponent(g:Graphics2D) = {
    g.drawImage(combinedBuffer, 0, 0, this.size.width, this.size.height, null)
  }

  def drawPatch(actorData:ActorData) = {
    patchBuffer.synchronized {
      val g = patchBuffer.getGraphics().asInstanceOf[Graphics2D]
      val x = actorData.getPosition()._1.toInt
      val y = actorData.getPosition()._2.toInt
      g.setColor(java.awt.Color.green)
      g.drawLine(x, y, x, y)

      val scentAge = actorData.getProperty("marten_scent_age")
      if (scentAge != null) {
        val scentiness = (128 *  1- ( (14 - scentAge.asInstanceOf[Int])/15)   ).toInt
        g.setColor(new Color(0, 0, 255, scentiness))
        g.drawLine(x, y, x, y)
      }


    }
  }

  def drawAgent(actorData:ActorData) = {
    agentBuffer.synchronized {
      val g = agentBuffer.getGraphics()
      val x = actorData.getPosition()._1.toInt
      val y = actorData.getPosition()._2.toInt
      g.setColor(java.awt.Color.red)
      g.drawLine(x, y, x, y)
    }
  }
}