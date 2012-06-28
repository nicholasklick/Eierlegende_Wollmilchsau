require 'java'

require 'hacks'
java_import 'akka.actor.ActorSystem'
java_import 'shapiro.netfauxgo.AddAgent'
java_import 'shapiro.netfauxgo.MovableAgent'

java_import 'Driver'   #this will spawn a world
world = Driver.world
actor_system = Driver.system

class Circler < MovableAgent
  def tick
    forward 2
    turn_right 2
  end
end

class Spacer < MovableAgent
  def initialize(*args)
    super
  end
  
  def tick
    nearby_agents = get_other_agents_in_vicinity 1
    wiggle if nearby_agents.length > 1

    ##How to query another dude's biz
    # if nearby_agents.length > 1
    #   other_x = get_actor_position(nearby_agents.first)._1
    #   puts "nearby guy has x = #{other_x}"
    # end
  end
  
  def wiggle
    forward 1
    turn_left 1
  end
end

puts "Spawning agents"
(world.width * world.height / 2).times do
  #print "."  #We should switch this to using the progress bar thing soon.
  new_actor = actor_system.actor_of(Props.create { Spacer.new world })
  world.manager.tell AddAgent.new new_actor
end

Driver.start_ticking