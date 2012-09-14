require 'yaml'
require 'active_record'

dbconfig = YAML.load(File.read('config/database.yml'))
ActiveRecord::Base.establish_connection dbconfig["development"]

require 'models/world'
require 'models/megatile'
require 'models/resource_tile'

