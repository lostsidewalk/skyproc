SkyProc is a Java library that offers easy to use Java API for importing, manipulating, and exporting mods.

The SkyProc library offers Java programmers the power to create and edit objects that represent Skyrim mods and records. It is able to import mods, or even an entire load order, and give easy access to the records inside. Programmers can then make any changes they wish, and export a working Skyrim patch that is customized to every user's load order.

Its purpose is to facilitate third party creation of smart programs that create custom patches based on any given load order. For many mods, this will help reduce, or completely eliminate conflicts. 

This is the continuing development and maintenance fork from tstavrianos's skyproc GitHub repository (https://github.com/tstavrianos/skyproc).

SkyProc Unified Manager 
- SUMprogram 
- Runs the SkyProc Unified Manager GUI 

Embedded script generation 
- SUMprogram -EMBEDDEDSCRIPTGEN
- Parses a list of Skyrim functions (Validation Files/EmbeddedScriptSource.txt) into Java enumerations (EmbeddedScriptsOut.txt).  
- These enums are used throughout SkyProc, and projects implementing this framework. 
- Future work: relocate this task to the build system (gradle) 

Patch generation (only) 
- SUMprogram -GENPATCH 
- Requires "SUM patch list.txt" to provide a list of SkyProc patches to merge 

Environment variables: 

All run configurations require at least two environment variables: 

   LOCALAPPDATA, which defines the path to 'plugins.txt', which should be managed by your mod manager (i.e., MO2)

   and SP_GLOBAL_PATH_TO_INI, which defines the path to Skyrim.ini.

Examples (from my Linux host using MO2 w/default profile): 

   LOCALAPPDATA=/home/me/Games/mod-organizer-2-skyrimspecialedition/modorganizer2/profiles/Default

   SP_GLOBAL_PATH_TO_INI=/home/me/.steam/debian-installation/steamapps/compatdata/489830/pfx/drive_c/users/steamuser/Documents/My Games/Skyrim Special Edition/Skyrim.ini

Configuration properties (application.properties):

The following configuration properties are required: 

   sp.global.path.to.data, which defines the path to the SSE 'Data' folder.

Example: 

   sp.global.path.to.data=/home/me/.steam/debian-installation/steamapps/common/Skyrim Special Edition/Data/

---

End users will likely invoke SkyProc through a mod that implements this framework, i.e., ASIS. 

