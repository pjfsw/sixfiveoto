.cpu _65c02
.encoding "ascii"

.const D = $c000
.const AY = $c001
.const AX = $c002
.const PAGE = $c003
.const LENGTH = $c004
.const SKIP = $c005
.const SCR_BG = $c006
.const CTRL_REG = $c007
.const SCREEN_PAGE = 0
.const COLOR_PAGE = 1
.const CTRL_PAGE = 2

.const CTRL_SCRX = 0
.const CTRL_SCRY = 2

.const SCROLL_LIMIT=36

.const BGCOLOR = %000110
.const TEXTCOLOR = %111111
.const MAX_LINE_LENGTH = 47

.const SPI_VIA = $c800
.const SS_PORT = SPI_VIA
.const SPI_PORT = SPI_VIA+1
.const SS_DDR = SPI_VIA+2
.const SPI_DDR = SPI_VIA+3

* = $E000 "ROM"

#import "readline.asm"
#import "print.asm"
#import "command.asm"

start:
    sei
    jsr setupPorts
    stz LENGTH
    stz SKIP
    jsr startupScreen

    stz cursorX
    lda #1
    sta cursorY
    cli

!:
    lda #'$'
    jsr printChar
    lda #' '
    jsr printChar
    jsr readline
    jsr parseCommand
    jmp !-

startupScreen:
    lda #BGCOLOR
    sta SCR_BG

    jsr clearScreen

    ldx #<message
    ldy #>message
    lda #messageLength
    jsr printLine
    rts

fillPage:
    stz AX
    stz AY

    ldx #15
!:
    ldy #0
    {
    !:
        sta D
        iny
        bne !-
    }
    dex
    bpl !-
    rts

clearScreen:
    stz PAGE
    lda #' '
    jsr fillPage

    lda #COLOR_PAGE
    sta PAGE
    lda #TEXTCOLOR
    jsr fillPage

    stz cursorX
    stz cursorY
    rts

callAddress:
    rts

irq:
    stx irqX
    sty irqY
    sta irqA
    stz CTRL_REG
    inc cursorBlink

    ldx irqX
    ldy irqY
    lda irqA
    rti

setupPorts:
    stz SPI_PORT
    lda #%01000001
    sta SPI_DDR
    lda #$FF
    sta SS_PORT
    lda #%00001111
    sta SS_DDR
    rts

message:
    .text "JOFMODORE 1.0 (C) 2020-2021 Johan Fransson"
.label messageLength = *-message
    .byte 0

rowToPixelLo:
    .fill 64,<(i*8)
rowToPixelHi:
    .fill 64,>(i*8)

* = $FFFA "Vectors"
    .word 0
    .word start
    .word irq

//.label CODE=*

*=$00 "zp" virtual
.zp {
ioAddress:
    .word 0
ioCount:
    .byte 0
 }

*=$0200 "Monitor RAM space" virtual
irqX:
    .byte 0
irqY:
    .byte 0
irqA:
    .byte 0
cursorX:
    .byte 0
cursorY:
    .byte 0
cursorBlink:
    .byte 0
readBuffer:
    .fill 64,0
readBufferSize:
    .byte 0
bg:
    .byte 0
scrollOffset:
    .byte 0
commandCount:
    .byte 0
inputOffset:
    .byte 0
jumpPointer:
    .word 0
argumentCount:
    .byte 0
