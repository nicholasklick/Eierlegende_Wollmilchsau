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

class MartenPatch < Patch
  # use hacks.rb set/get property
  def vole_population
    get_property 'vole_population'
  end

  def vole_population=(value)
    set_property 'vole_population', value
  end

  def marten
    get_property 'marten'
  end

  def marten=(value)
    set_property 'marten', value
  end

  def marten_scent_age
    get_property 'marten_scent_age'
  end

  def marten_scent_age=(value)
    set_property 'marten_scent_age', value
  end
  
  def tree_density
    get_property 'tree_density'
  end
  
  def tree_density=(value)
    set_property 'tree_density', value
  end


  def initialize(world, world_id, x, y)
    super world, x, y
    
    correspondent = ResourceTile.where(:world_id => world_id, :x => x, :y => y).first
    
    self.tree_density = correspondent.tree_density
    self.vole_population = 0
    self.marten = nil
    self.marten_scent_age = nil
    #puts "Patch (#{self.object_id}) initialized with correspondent (#{correspondent.id}) tree_density #{self.tree_density}"
  end

  def tick
    grow_vole_population
    smelly_stuff
  end

  def grow_vole_population
    self.vole_population *= 1.01
  end

  def smelly_stuff
    unless self.marten_scent_age.nil?
      self.marten_scent_age += 1
      if self.marten_scent_age > 14
        self.marten_scent_age = nil
        self.marten = nil
      end
    end
  end
end

class DefaultPatchSpawnerInRuby < PatchSpawner
  def initialize(world_id)
    super() #parens needed!
    @world_id = world_id
  end
  
  def spawnPatch(world, x, y)
    MartenPatch.new(world, @world_id, x, y)
  end
end

db_world_id = ARGV.first.to_i

puts "Using World ID #{db_world_id}"
db_world = World.find db_world_id

patchSpawner = DefaultPatchSpawnerInRuby.new(db_world_id)
driver = Driver.new(db_world.width, db_world.height, patchSpawner)
world = driver.world
actor_system = driver.system

class Marten < MovableAgent
  def initialize(*args)
    super
  end

  def tick
    # do it
    eat_all_the_voles
    go_to_that_place
    leave_your_paw_smells
    die_now
    have_babies
  end


  def eat_all_the_voles
    patch = current_patch
    population = get_actor_property patch, 'vole_population'
    population *= 0.75
    set_actor_property patch, 'vole_population', population
  end


  def go_to_that_place
    patches = getPatchesInNeighborhood(1)
    # not using it to decide direction just yet
    #patches.for_each do |patch|
    #  get_actor_property patch, 'marten'
    #end

    turn_right rand(360)
    forward 1
  end

  def leave_your_paw_smells
    patch = current_patch
    set_actor_property patch, 'marten', self
    set_actor_property patch, 'marten_scent_age', 0
  end

  def die_now
    die if rand > 0.98
  end

  def have_babies
    if rand > 0.98
      new_actor = world.system.actor_of(Props.create { Marten.new world })
      world.manager.tell AddAgent.new new_actor
    end
  end
end

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
200.times do
  new_actor = actor_system.actor_of(Props.create { Marten.new world })
  world.manager.tell AddAgent.new new_actor
end

driver.start_ticking
actor_system.await_termination