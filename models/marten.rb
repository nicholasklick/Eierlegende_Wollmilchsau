class Marten < RubyMovableAgent
  stm_attr_accessor :energy
  Max_Energy = 3334
  
  def initialize(*args)
    super
    self.energy = Max_Energy/2
  end

  def tick
    eat_a_vole_if_you_can
    go_to_a_better_place
    leave_your_paw_smells
    die_maybe
    have_babies_maybe
  end

  def eat_a_vole_if_you_can
    # assumes 1.52712 encounters per step - equivalent to uncut forest encounter rate from Andruskiw et al 2008
    # probability of kill in uncut forest 0.05 (calculated from Andruskiw's values; 0.8 kills/24 encounters)
    # probability of kill in 1 step = 1.52712 * 0.05 = 0.076356
    
    return if self.energy > Max_Energy
    
    patch = current_patch
    vole_population = get_actor_property patch, 'vole_population'
    p_kill = 0.076356 * (vole_population / DeerMartenPatch::Max_vole_population)
    
    if rand > (1 - p_kill)
      self.energy = energy + 1
      set_actor_property patch, 'vole_population', (vole_population - 1)
    end
  end

  def go_to_a_better_place
    patches = get_patches_in_neighborhood(1).sort_by {|patch| desirability(patch)} 
    most_desirable = patches.reverse.first
    #puts "Most desirable patch's desirability is #{desirability(most_desirable)}"
    turn_toward most_desirable
    forward 1
    self.energy -= 1
  end
  
  def desirability(a_patch)
    treeness_accum = 0
    multiplier = 1
    if get_actor_property(a_patch, "tree_type") == "coniferous"
      DeerMartenPatch::Tree_fields.each do |field|
        #count bigger trees more than littler trees
        treeness_accum += multiplier * get_actor_property(a_patch, field.to_s)
        multiplier += 2
      end
    end
    
    marten_whose_scent_is_here = get_actor_property a_patch, "marten"
    if marten_whose_scent_is_here and marten_whose_scent_is_here != self
      other_marten_penalty = 1000
    else
      other_marten_penalty = 0
    end
    
    treeness_accum - other_marten_penalty   
  end
  
  def die_maybe
    die if rand > 0.98 or energy <= 0
  end

  def have_babies_maybe
    if rand > 0.98 
      offspring = spawn Marten
      self.energy = self.energy / 2
    end
  end
  
  def leave_your_paw_smells
    patch = current_patch
    set_actor_property patch, 'marten', self
    set_actor_property patch, 'marten_scent_age', 0
  end

end
