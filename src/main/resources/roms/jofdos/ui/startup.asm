#importonce

#import "clearscreen.asm"

.segment DosCode

startupScreen:
    lda #<pico_screen_pal
    sta AL
    lda #>pico_screen_pal
    sta AH
    ldx #BGCOLOR
    ldy #TEXTCOLOR
    // First color palette is text color on background color
    stx D
    sty D
    stz D
    stz D
    // Second palette is reversed
    sty D
    stx D
    stz D
    stz D

    jsr clearScreen

    jsr setPosition
    ldx #<message
    ldy #>message
    lda #messageLength
    jsr printLine

    ldx #<message2
    ldy #>message2
    lda #message2Length
    jsr printLine
    rts

message:
    .text "Welcome to JOFMODORE/JOFDOS 1.0"
.label messageLength = *-message
    .byte 0

message2:
    .text "Copyright 2020-2021 PJFSW"
.label message2Length = *-message2
