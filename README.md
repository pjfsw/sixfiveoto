SixFiveOTo
==========
Emulator of the WDC 65C02 CPU with plugin support for hardware modules

Usage
-----

java SixFiveOTo <PRG-file>

Launch PRG file that is compiled between $F000-$FFFF

Recommended tool to assemble: Kick Assembler

F8 - Suspend/resume
F6 - Step

Planned TODOs
-------------

* Partial 65C22 support (GPIO) 
* SPI emulation 
* Gameduino V1 emulation
* Serial EEPROM emulation
* Serial RAM emulation
* LCD display emulation
* Keyboard matrix emulation 

Information sources
-------------------
https://www.masswerk.at/6502/6502_instruction_set.html
http://6502.org/tutorials/65c02opcodes.html
http://www.righto.com/2012/12/the-6502-overflow-flag-explained.html
