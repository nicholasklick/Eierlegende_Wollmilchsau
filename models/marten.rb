class Marten < RubyMovableAgent
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
