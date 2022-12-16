# Tally
Originally used on VectorMC for statistics tracking alongside Atlas, this project has expanded to include statistics tracking from several sources and games, and hopes to centralize statistics tracking.

## Core vs Snap In
- Core = the basic stats needed to track in the server software this core plugin is built for
- [Snap In](https://github.com/jasoryeh/TallySnapins) = extensions that hook into core to support custom software

## Functionality
As you might notice, this library/api interacts with another api. I went this route to reduce the types of storage needed to be implemented and to just settle on one statistics platform.

## Where is the server?
Server is your website that accepts API requests! Currently, this is to be implemented by a developer wishing to adopt Tally as a standard for statistics communication. A plan for a public server for unified statistics is on the way, however if you wish to use Tally and implement statistics for your own use, here is an informal document describing what the library expects: [Tally v2 Specification](https://docs.google.com/document/d/1aqecWjgmHaggyBuT0UKb_FCLyzDJMIoXnXwFAA1JoAE/edit?usp=sharing).

### Updates
Updates will be provided in my free time from school. Feel free to make pull requests.

## How to fork this for non-Java and non-Minecraft
This is a generic library for implementation of statistics for Minecraft server forks such as Paper and Bukkit, and potentially independent Java applications, usage of its functions depend on additional plugins that depend on it which are currently called Snap-Ins, examples of such are located [here](https://github.com/jasoryeh/TallySnapins) for PGM and Atlas.
