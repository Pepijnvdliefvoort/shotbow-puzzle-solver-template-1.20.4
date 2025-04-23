# Introduction
 This mod is designed for the Shotbow Network server, specifically for the gamemode MineZ.
 
# Features
Currently, only the dungeon St. Roseluck Crypt has a solve implemented.
In the boss room, it automatically detects the blocks which should be pressed.

If the blocks aren't appearing behind the button, you can manually configure the coordinates.
See [Commands](#Commands) for more information.

# Commands
```
# Select the bottom left block of the quartz 5x5 wall
/puzzlesolver set quartz_start x y z

# Select the bottom right block of the button wall (block behind the button)
/puzzlesolver set button_start x y z

# Select the trigger button (the one that starts the boss room, i.e. creates a pattern)
/puzzlesolver set trigger x y z

# Select the submit button
/puzzlesolver set clear x y z
```

# Building
* Run `./gradlew build`
* Copy `build/libs/shotbow-puzzle-solver-1.0.0.jar` to your mods folder
