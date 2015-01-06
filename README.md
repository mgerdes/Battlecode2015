Managing Tech Tree
------------------
* Have a class that has a list of all robots (units and structures)
* Beavers will mine and build buildings in that order
* This will allow us to change our overall strategy at compile time, later we will need to be able to adjust strategy at run time
* List example: Beaver, Beaver, Beaver, MiningStation, Miner, Miner, Barracks, Soldier, Soldier, Soldier, TankStation, etc...


Macro Strategy Ideas
--------------------
* Defend and try to win via tiebreaker
* Win by destroying enemy HQ
* Waste enemy resources by destroying buildings and pestering miners
* Build units that best respond to opponents units
* Use towers for distributed algorithms?
* Use scout units (that may die) to see what buildings enemy has

Micro Strategy Ideas
--------------------
* Use robots with greater range than enemy and quickly move away after attacking
* Avoid clustering when facing enemy weapons that have proximity damage
* Find optimal mixes of robots to minimize weaknesses
* Attack the enemy robot that will die first (lower HP, less armor, etc...)
* Transfer supplies from a unit before it dies

Small map vs big map
--------------------
* Harder to rush on big map

Using for spare bytecodes
-------------------------
* Broadcast bug paths (towers can potentially do this)
* Optimize bug paths that have been broadcast
