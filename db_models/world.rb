class World < ActiveRecord::Base
  has_many :megatile
  has_many :resource_tiles
end