java_import 'scala.None'

class Marten < RubyMovableAgent
  stm_attr_accessor :energy
  Max_Energy = 3334
  
  Class_codes_martens_can_dig = [41, 42, 43, 90] #conifers + mixed
  
  def initialize(*args)
    super
    self.energy = Max_Energy
    # puts "** initialized marten with position #{data.getPosition._1} #{data.getPosition._2} and energy #{self.energy} **"
  end

  def tick
    self.energy = [0,self.energy - Max_Energy/7].max # a full marten can go a week without eating.
    # puts "Marten energy = #{self.energy}"
    eat_a_vole_if_you_can
    have_babies_maybe
    go_to_a_better_place
    leave_your_paw_smells
    die_maybe
  end

  def eat_a_vole_if_you_can
    #### the following is not true
    # assumes 1.52712 encounters per step - equivalent to uncut forest encounter rate from Andruskiw et al 2008
    # probability of kill in uncut forest 0.05 (calculated from Andruskiw's values; 0.8 kills/24 encounters)
    # probability of kill in 1 step = 1.52712 * 0.05 = 0.076356
    
    return if self.energy > Max_Energy
    
    patch = current_patch
    vole_population = [0, get_actor_property(patch, 'vole_population')].max
    p_kill = (vole_population / DeerMartenPatch::Max_vole_population) * 3.0
    
    if rand > (1 - p_kill)
      self.energy += Max_Energy/2
      set_actor_property(patch, 'vole_population', vole_population - 1)
    end
  end

  def go_to_a_better_place
    patches = get_patches_in_neighborhood(3).find_all do |patch|
      none_or_my_scent =  [None, nil, agentID].include? get_actor_property(patch, "marten")
      none_or_my_scent && Class_codes_martens_can_dig.include?(get_actor_property(patch, "landcover_class_code"))
    end
    
    if patches.count == 0
    else    
      filtered_patches = patches.sort_by {|patch| desirability(patch)} 
      most_desirable = filtered_patches.slice(0,2).shuffle.first # add some noise to the selection of the best patch
      #puts "Most desirable patch's desirability is #{desirability(most_desirable)}"
      #desirability(most_desirable, true)
      turn_toward most_desirable
      forward 1
      self.energy -= 10
    end
  end
  
  def desirability(a_patch, print_stats = false)
    treeness_accum = 0
    multiplier = 1
    coniferous_bonus = case get_actor_property(a_patch, "landcover_class_code")
    when 42, 43
      500
    else
      0
    end
    DeerMartenPatch::Tree_fields.each do |field|
      #count bigger trees more than littler trees
      treeness_accum += multiplier * get_actor_property(a_patch, field.to_s)
      multiplier *= 1.1
    end
    treeness_accum = [treeness_accum, 500].min
  
    marten_whose_scent_is_here = get_actor_property a_patch, "marten"
    marten_scent_age = get_actor_property a_patch, 'marten_scent_age'
    # other_marten_penalty = 0
    # if marten_whose_scent_is_here != None and marten_whose_scent_is_here != getSelf
    #   other_marten_penalty = 1100 if self.energy > Max_Energy/8 #starving overrides territoriality
    # end
    # 
    backtrack_penalty = 0
    if marten_whose_scent_is_here and marten_whose_scent_is_here != None and marten_scent_age == 0 
      backtrack_penalty = 500
    end
    
    voleness = (get_actor_property(a_patch, 'vole_population') / DeerMartenPatch::Max_vole_population) * 100 
    puts "Desirability: voleness=#{voleness} treeness=#{treeness_accum} other_marten_penalty=#{other_marten_penalty} coniferous_bonus=#{coniferous_bonus}" if print_stats and other_marten_penalty>0
    
    voleness + treeness_accum + coniferous_bonus - backtrack_penalty # - other_marten_penalty 
  end
  
  def die_maybe
    die if rand >= 0.99 or energy <= 0
  end

  def have_babies_maybe
    if rand > 0.98 and self.energy > Max_Energy/2
      my_pos = position
      offspring = spawn(Marten, (x + 2-rand(5)) % world.width, ((y + 2 - rand(5)) %world.height))
      self.energy = self.energy / 2
    end
  end
  
  def leave_your_paw_smells
    patch = current_patch
    set_actor_property patch, 'marten', agentID
    set_actor_property patch, 'marten_scent_age', 0
  end

end


