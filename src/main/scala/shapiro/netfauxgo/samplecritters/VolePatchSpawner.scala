package shapiro.netfauxgo.samplecritters

import shapiro.netfauxgo.{PatchSpawner, World}

class VolePatchSpawner extends PatchSpawner {
	def spawnPatch(world:World, x:Int, y:Int) = {
		new VolePatch(world, x, y)
	}	
}