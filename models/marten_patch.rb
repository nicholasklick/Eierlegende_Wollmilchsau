class MartenPatch < RubyPatch  
  stm_attr_accessor :vole_population, :marten, :marten_scent_age, :tree_density
  sync_fields :tree_density

  def initialize(world, world_id, x, y)
    super world, x, y
    correspondent = ResourceTile.where(:world_id => world_id, :x => x, :y => y).first
    use_correspondent correspondent
    
    self.tree_density = correspondent.tree_density
    self.vole_population = 0
    self.marten = nil
    self.marten_scent_age = nil
    #puts "Patch (#{self.object_id}) initialized with correspondent (#{correspondent.id}) tree_density #{self.tree_density}"
  end

  def tick
    grow_vole_population
    smelly_stuff
    #sync_db_data  #slow to do this here, better to use the reporter functionality
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
