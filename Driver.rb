require 'database_magic'
require 'java'
require 'hacks'
java_import 'akka.actor.ActorSystem'
java_import 'shapiro.netfauxgo.AddAgent'
java_import 'shapiro.netfauxgo.MovableAgent'
java_import 'shapiro.netfauxgo.PatchSpawner'
java_import 'shapiro.netfauxgo.Patch'
java_import 'Driver'  

# TODO
# * landcover
# * import
# * map render
# * decide movement based on neighborhood patches
# * reproduce
# * die


require 'models/marten_patch'
require 'models/marten'


class DefaultPatchSpawnerInRuby < PatchSpawner
  def initialize(world_id)
    super() #parens needed!
    @world_id = world_id
  end
  
  def spawnPatch(world, x, y)
    DeerMartenPatch.new(world, @world_id, x, y)
  end
end

db_world_id = ARGV.first.to_i

puts "Using World ID #{db_world_id}"
db_world = World.find db_world_id
puts "\t width = #{db_world.width}; height = #{db_world.height}"

patchSpawner = DefaultPatchSpawnerInRuby.new(db_world_id)
driver = Driver.new(db_world.width, db_world.height, patchSpawner)
world = driver.world
actor_system = driver.system


puts "Spawning agents"
200.times do
  new_actor = actor_system.actor_of(Props.create { Marten.new world })
  world.manager.tell AddAgent.new new_actor
end

driver.start_ticking
actor_system.await_termination