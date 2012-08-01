package shapiro.netfauxgo

import concurrent.stm._
import akka.actor.ActorRef

class ActorData(val actor: ActorRef, val klass:String) {
  private var stuff = TMap.empty[String,Any]
  private val x = Ref[Double](0)
  private val y = Ref[Double](0)
  private val dead = Ref(false)

  def setX(newX:Double) = {
    x.single() = newX
  }

  def setY(newY:Double) = {
    y.single() = newY
  }

  def setPosition(newX:Double, newY:Double):Unit = atomic { implicit txn =>
    x() = newX
    y() = newY
  }

  def setPosition(loc:Tuple2[Double, Double]):Unit = {
    setPosition(loc._1, loc._2)
  }

  def getPosition() = atomic { implicit txn =>
    (x.get, y.get)
  }

  def setProperty(key:String, value:Any) = {
    stuff.single += (key -> value)
//    if (key == "heading")
//      println(actor.toString + " set property " + key + " = " + value)
  }

  def getProperty(key:String) = {
    stuff.single(key)
  }

  def die() = {
    dead.single() = true
  }

  def isDead() = {
    dead.single.get
  }

  override def clone():ActorData = atomic { implicit txn =>
    val theClone = new ActorData(actor, klass)
    theClone.x.set(x.get)
    theClone.y.set(y.get)
    theClone.dead.set(dead.get)
    theClone.stuff = stuff.clone
    theClone
  }
}
