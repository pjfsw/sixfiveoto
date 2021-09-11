#importonce

#import "string.asm"

.segment DosCode

callAddress:
    jsr argToHex
    bcc !+
    jmp valueError
!:
    stx ioAddress
    sty ioAddress+1
    jmp (ioAddress)
