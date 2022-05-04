SkyProc is a Java library that offers easy to use Java API for importing, manipulating, and exporting mods.

The SkyProc library offers Java programmers the power to create and edit objects that represent Skyrim mods and records. It is able to import mods, or even an entire load order, and give easy access to the records inside. Programmers can then make any changes they wish, and export a working Skyrim patch that is customized to every user's load order.

Its purpose is to facilitate third party creation of smart programs that create custom patches based on any given load order. For many mods, this will help reduce, or completely eliminate conflicts. 

This is the continuing development and maintenance fork from Leviathan's Google Code repository since he no longer has time to work on it. It is currently maintained by DienesToo.

SkyProc Unified Manager 
- SUMprogram 
- Runs the SkyProc Unified Manager GUI 

Embedded script generation 
- SUMprogram -EMBEDDEDSCRIPTGEN
- Parses a list of Skyrim functions (Validation Files/EmbeddedScriptSource.txt) into Java enumerations (EmbeddedScriptsOut.txt).  
- These enums are used throughout SkyProc, and projects implementing this framework. 
- Future work: relocate this task to the build system (gradle) 

SkyProc live import/patch generation test module 
- SUMprogram -TESTIMPORT
- Imports the full mod list and generates a test patch.

Patch generation (only) 
- SUMprogram -GENPATCH 
- Requires "SUM patch list.txt" to provide a list of SkyProc patches to merge 

End users will likely invoke SkyProc through a mod that implements this framework, i.e., ASIS. 