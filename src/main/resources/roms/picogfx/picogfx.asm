.cpu _65c02
.encoding "ascii"

.const IRQ = $0300
.const SCREEN_PAGE = 0
.const COLOR_PAGE = 1
.const CTRL_PAGE = 2

.const CTRL_SCRX = 0
.const CTRL_SCRY = 2

.const SCROLL_LIMIT=36

.const BGCOLOR = %000110
.const DERPESCOLOR = %111111
.const MAX_LINE_LENGTH = 47

.const SPI_VIA = $c800
.const SS_PORT = SPI_VIA
.const SPI_PORT = SPI_VIA+1
.const SS_DDR = SPI_VIA+2
.const SPI_DDR = SPI_VIA+3

* = $E000 "ROM"

#import "picoreg.asm"
.const AY = AH
.const AX = AL

#import "readline.asm"
#import "print.asm"
#import "command.asm"
#import "string.asm"
#import "loader.asm"
start:
    jsr setupPorts
    jsr load_rom
    jsr LOAD_TARGET
    jsr initTheDerpes
    jsr copyIrq
    jmp *
/*
    jsr startupScreen

    lda #$80
    sta PAGE
    lda #$08
    sta AY
    lda #$ff
    sta D
    stz cursorX
    lda #1
    sta cursorY

!:
    lda #'$'
    jsr printChar
    lda #' '
    jsr printChar
    jsr readline
    jsr parseCommand
    jmp !-
*/
initTheDerpes:
    lda #0
    ldx #15
!:
    sta sinPos,x
    clc
    adc #2
    dex
    bne !-


    // Poke a custom font value in font 1
    lda #<(pico_font_1 + 16 * 32)
    sta AL
    lda #>(pico_font_1 + 16 * 32)
    sta AH
    ldx #255
    lda #$f0
!:
    sta D
    dex
    bne !-


    // Set a different font for row 35
    lda #<(pico_font_select + 35)
    sta AL
    lda #>(pico_font_select + 35)
    sta AH
    lda #1
    sta D
    rts
copyIrq: // IRQ vector is hardwired to point at RAM address so we need to copy ours to RAM
    ldx #0
!:
    lda irqSource,x
    sta IRQ,x
    inx
    cpx #irqLength
    bcc !-
    cli
    rts

startupScreen:
    lda #DERPESCOLOR
    sta textColor

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

.print "Clearscreen = " + toHexString(*)
clearScreen:
    rts

argToHex:
    lda argumentLength
    ldx #<argument1
    ldy #>argument1
    jmp readHexString

pokeByte:
    lda argumentLength+1
    ldx #<argument2
    ldy #>argument2
    jsr readHexString
    bcc !+
    jmp valueError

!:
    stx peekAddress
    sty peekAddress+1
    jsr argToHex
    bcc !+
    jmp valueError
!:
    txa
    sta (peekAddress)
    rts

peekByte:
    jsr argToHex
    bcc !+
    jmp valueError
!:
    stx ioAddress
    sty ioAddress+1
    lda (ioAddress)
    jsr printByte
    jmp linefeed

peekPage:
    jsr argToHex
    bcc !+
    jmp valueError
!:
    stz peekAddress
    sty peekAddress+1
    ldy #0
!nextByte:
    tya
    and #$07
    bne !+
    jsr printPeekAddress
!:
    lda (peekAddress),y
    phy
    jsr printByte
    lda #' '
    jsr printChar
    ply
    iny
    tya
    and #$07
    beq !printAscii+
    jmp !nextByte-

!printAscii:
    tya
    sec
    sbc #8
    tay
!nextAscii:
    lda (peekAddress),y
    phy
    jsr printChar
    ply
    iny
    tya
    and #$07
    bne !nextAscii-
    phy
    jsr linefeed
    ply
    tya
    bne !nextByte-
    rts

printPeekAddress:
    phy
    lda peekAddress+1
    jsr printByte
    pla
    pha
    jsr printByte
    lda #' '
    jsr printChar
    ply
    rts



callAddress:
    jsr argToHex
    bcc !+
    jmp valueError
!:
    stx ioAddress
    sty ioAddress+1
    jmp (ioAddress)

setBgColor:
    /*jsr argToHex
    bcc !+
    jmp valueError
!:
    stx SCR_BG*/
    rts

setFgColor:
    jsr argToHex
    bcc !+
    jmp valueError
!:
    stx textColor
    rts

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

valueErrMsg:
    .text "Argument must be 1-4 byte hex!"
.label valueErrLength = *-valueErrMsg

rowToPixelLo:
    .fill 64,<(i*8)
rowToPixelHi:
    .fill 64,>(i*8)
sinTableLo:
    .fill 256, <(300+100*sin(i*PI/128))
sinTableHi:
    .fill 256, >(300+100*sin(i*PI/128))

bmpSinTable:
    .fill 256, 20+20*sin(i*PI/128)

.macro scrollX(address) {
    lda #$62
    sta AL
    lda #$64
    sta AH
    lda address
    sta D
    lda address+1
    sta D
}

irqSource:
.pseudopc IRQ {
irq:
    stx irqX
    sty irqY
    sta irqA

    lda #0
    sta PAGE
    lda #<pico_bitmap_start
    sta AL
    lda #>pico_bitmap_start
    sta AH
    ldy sinPos
    lda bmpSinTable,y
    sta D

    inc scrollOffset
    bne !+
    inc scrollOffset+1
!:
    scrollX(scrollOffset);

    lda #<pico_sprite_y
    sta AL
    lda #>pico_sprite_y
    sta AH

    ldx #15
!:
    inc sinPos,x
    lda sinPos,x
    tay
    lda sinTableLo,y
    sta D
    lda sinTableHi,y
    sta D
    dex
    bpl !-

    /*.const screenSelectReg = $06464;
    lda #<screenSelectReg
    sta AL
    lda #>screenSelectReg
    sta AH
    inc screenSelect
    lda screenSelect
    rol
    rol
    and #1
    sta D*/


    ldx irqX
    ldy irqY
    lda irqA
    rti
}
.label irqLength = *-irqSource

* = $FFFA "Vectors"
    .word 0
    .word start
    .word IRQ

//.label CODE=*

*=$00 "zp" virtual
.zp {
ioAddress:
    .word 0
ioCount:
    .byte 0
peekAddress:
    .word 0
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
textColor:
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
currentByte:
    .byte 0
currentNibble:
    .byte 0
tmpNumber:
    .word 0
argument1:
    .fill MAX_LINE_LENGTH,0
argument2:
    .fill MAX_LINE_LENGTH,0
argumentLength:
    .byte 0,0
screenSelect:
    .byte 0
sinPos:
    .fill 16,i