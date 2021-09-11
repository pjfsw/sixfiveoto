.cpu _65c02
.encoding "ascii"

.const IRQ = $0300
.const SCREEN_PAGE = 0
.const COLOR_PAGE = 1
.const CTRL_PAGE = 2

.const CTRL_SCRX = 0
.const CTRL_SCRY = 2

.const SCROLL_LIMIT=36

.const MAX_LINE_LENGTH = 47

.const SPI_VIA = $c800
.const SS_PORT = SPI_VIA
.const SPI_PORT = SPI_VIA+1
.const SS_DDR = SPI_VIA+2
.const SPI_DDR = SPI_VIA+3

* = $E000 "ROM"

.const BGCOLOR = %010101
.const TEXTCOLOR = %111111

#import "dir.asm"
#import "picoreg.asm"
#import "readline.asm"
#import "print.asm"
#import "command.asm"
#import "string.asm"
#import "loader.asm"
#import "utility/clearscreen.asm"

start:
    jsr setupPorts
    jsr copyIrq
    cli
    jsr startupScreen

    stz cursorX
    lda #2
    sta cursorY

!:
    lda #'$'
    jsr printChar
    lda #' '
    jsr printChar
    jsr readline
    jsr parseCommand
    jmp !-

copyIrq: // IRQ vector is hardwired to point at RAM address so we need to copy ours to RAM
    ldx #0
!:
    lda irqSource,x
    sta IRQ,x
    inx
    cpx #irqLength
    bcc !-
    rts

startupScreen:
    stz cursorX
    stz cursorY
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

    // Restore font pointers, sprites and bitmaps
    lda #<pico_sprite_x
    sta AL
    lda #>pico_sprite_x
    sta AH
    ldx #$c0 // Repeat 192 times
!:
    stz D
    dex
    bne !-

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

clear:
    jsr clearScreen
    lda #<pico_screen_select
    sta AL
    lda #>pico_screen_select
    sta AH
    stz D
    lda #<pico_bitmap_start
    sta AL
    stz D
    stz D
    stz D

    stz cursorX
    stz cursorY
    lda #<pico_scroll_y
    sta AL
    lda #>pico_scroll_y
    sta AH
    stz D
    stz D
    stz scrollOffset
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
    .text "Welcome to JOFMODORE/JOFDOS 1.0"
.label messageLength = *-message
    .byte 0

message2:
    .text "Copyright 2020-2021 PJFSW"
.label message2Length = *-message2

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
    jmp irq
.label irqLength = *-irqSource

irq:
    stx irqX
    sty irqY
    sta irqA
    sta SAVEADDR

    inc cursorBlink

    sta RESTADDR
    ldx irqX
    ldy irqY
    lda irqA
    rti

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
loadSource:
    .word 0
loadTarget:
    .word 0
loadCount:
    .byte 0
stringSource:
    .word 0
stringTarget:
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
