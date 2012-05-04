class SpacerAgent(world:World) extends MovableAgent(world) {

  def tick() = {
    if (getAgentsOnMyPatch().length > 1){
      forward(1)
      turn_left(1)    
    }
  }  
}