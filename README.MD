# Tally
Originally used in VectorMC for statistics tracking on Atlas, this project is now expanded for general statistics tracking.

## Core vs Snap In
- Core = the basic stats needed to track in the server software this core plugin is built for
- [Snap In](https://github.com/jasoryeh/TallySnapins) = extensions that hook into core to support custom software

## Functionality
As you might notice, this library/api interacts with another api. I went this route to reduce the types of storage needed to be implemented and to just settle on one statistics platform.

## Where is the server?
Server is your website that accepts API requests! (A server for Tally is in the works, but not yet ready for the public, as it was written hastily for testing, rest assured it will come soon)

### Updates
Updates will be provided in my free time from school. Feel free to make pull requests.

## How to fork this for non-Java and non-Minecraft
Tally is in a primitive stage as it was cleaned up from multiple forks of the source code, for now, check out the [bukkit module](https://github.com/jasoryeh/TallyLib/tree/master/bukkitcore/src/main/java/tk/jasonho/tally/core/bukkit).

In short, although the Bukkit module is a generic library implementation of Tally, you only need to ensure you can implement the [models](https://github.com/jasoryeh/TallyLib/tree/master/api/src/main/java/tk/jasonho/tally/api/models), and to upload, ensure you have a [statistics manager](https://github.com/jasoryeh/TallyLib/blob/master/api/src/main/java/tk/jasonho/tally/api/TallyStatsManager.java).
