
### Defiance ###

* Quick summary
This is an enhanced version of the Sydney Engine multiplayer shooter game, written in Java originally by Keith Woodward in 2008, using the Apache Mina networking framework.

* Version 1.0.1

### Screenshots ###
![alt text](https://github.com/GreenLantern101/Defiance/blob/master/SydneyShooter/Defiance2.PNG "After which I succumbed to great firepower.")

### Setting Up and Playing ###

Dependencies: 
The jars are included in the SydneyDependencyJars folder.


======[ Controls ]====== 

Move with Arrow keys or W, A, S, D
Left mouse click: fires weapon
R: reloads weapon
Number keys (1-9) or Q/E or Mouse wheel: selects available weapons
`[funny key above TAB]: turn on/off player names and hit points
Enter or ShiftEnter: Activate chat, then and once you've typed the message, press enter again to send. 
If shift is down when you first press enter, the 'send to allies' option is ticked
Page up or down: zoom in/ out
Escape: displays menu.
				
======[ Weapons ]====== 

Pistol - Basic weapon with low damage.
Machine Gun - Standard rapid-firing rotating gatling.
Flamethrower - Close-range weapon that sprays napalm that will stick around for a while, useful for area denial. The napalm will cause damage to anyone that walks over it.
Shotgun - Dual-shot weapon that fires a cloud of pellets. Most effective at close ranges
Sniping Rifle - Accurate for long-distance one-hit kills, but slow-firing.
Tranquilizer Gun - Fires in short bursts, stunning enemies and slowing them and making them unable to fire.
Rocket Launcher - launches rockets that have a large explosion radius. More damage is dealt to objects that are closer to the epicenter of the explosion.
Homing Gun - launches three alien-technology orbs that will fire like a regular bullet unless there is an enemy in front of it, in which case it will automatically follow that target. The orbs are relatively slow, but are still useful around corners and against dug-in defenders.
Nail Gun - An alternative to the machine gun, that spits out 3 nails at once very rapidly, but watch out for your ammo depletion, since it comes with a small clip considering its rate of fire.

***Note***
Both the Flamethrower and the Rocket Launcher are Variable-Range weapons, that is, their bullets can detonate or stick at a  certain range away from the weapon by using the mouse to aim.
				
======[ Items ]====== 

Healthpack - Restores 33% health.
Godly Armor - Grants invincibility against all weapons for 15 seconds, but player can still be tranquilized.
Speed Shoes - Significantly speeds up travel.
Invisibility Shroud - Enemies can't see and target you for 15 seconds.

***Note***
You can only use one of either the Godly Armor or the Invisibility Shroud, but not both. But, the Speedy Shoes can be used with anything.
				
======[ Extra ]======  

The wi-fi on laptops may cause noticeable lag over local LAN, but still very playable. 
				
This was slightly modified from the excellent Sydney Engine v. 0.2, made by Keith Woodward,  whom can find me on the forums at www.javagaming.org or contact through keithphw@hotmail.com . If you'd like to expand on this game, you can download the source code of Sydney Engine on www.javagaming.org, just search for 'SydneyEngine'.



### Contribution guidelines ###

TODO:
Fix internet gameplay, make networking more dependable

Achievements??? - 1st kill, 3rd, 10th, 20th, 30th, 50th, 75th, 100th, 150th, 300th, 500th kills
Die 1 time, Die 3 times with no kills, Get 3 healthpacks, Get 3 invisibilities, Switch weapons 20 times, used each weapon at least once, killed with each weapon at least once, assist 25 times

Make personal pop-up alerts configurable

Enhance graphics

Mech Suits support?
Destructable walls- possibly by using subtractive polygons
Map Editor/Maker!

======= when adding a new Gun:
1. Make new gun, and bullet class
2. Update the statement in Player class of what killed description should be
3. Update all the itemSpawners in gameworld class to spawn it
4. Update bots to spawn it when respawning
5. Update help menu info

### Who do I talk to? ###

You can reach me at tiger201655@yahoo.com
