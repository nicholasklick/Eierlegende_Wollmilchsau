Dir["lib/\*.jar"].each { |jar| require jar }

require 'database_magic'
require 'java'
require 'hacks'
require 'PNG_reporter'

java_import 'akka.actor.ActorSystem'
java_import 'shapiro.netfauxgo.AddAgent'
java_import 'shapiro.netfauxgo.MovableAgent'
java_import 'shapiro.netfauxgo.PatchSpawner'
java_import 'shapiro.netfauxgo.Patch'
java_import 'shapiro.netfauxgo.TickReporter'
java_import 'shapiro.netfauxgo.RegisterTickReporter'
java_import 'Driver' 

require 'models/deer_marten_patch'
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

reporter = PNGReporter.new(world, db_world_id)
world.manager.tell RegisterTickReporter.new(reporter)

puts "Spawning agents"
how_many_martens_to_spawn = ((10.0/(128*128)) * db_world.width * db_world.height).floor
marten_tiles = db_world.resource_tiles.where(:landcover_class_code => Marten::Class_codes_martens_can_dig).order("RAND()").limit(how_many_martens_to_spawn)
marten_tiles.each do |rt|
  # puts "Spawning marten at #{rt.x}, #{rt.y}"
  new_actor = actor_system.actor_of(Props.create { Marten.new world, rt.x, rt.y })
  world.manager.tell AddAgent.new new_actor
end

driver.start_ticking
actor_system.await_termination
