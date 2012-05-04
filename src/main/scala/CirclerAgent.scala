class CirclerAgent(world:World) extends MovableAgent(world) {

  def tick() = {
    forward(1)
    turn_right(1)
  }  
}