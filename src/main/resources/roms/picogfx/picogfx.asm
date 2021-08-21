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

* = $E000

#import "readline.asm"

start:
    sei
    jsr setupPorts

    lda #BGCOLOR
    sta SCR_BG

    stz AX
    stz AY
    stz PAGE
    ldx 0
!:
    lda message,x
    beq !+
    sta D
    inx
    jmp !-
!:
    lda #COLOR_PAGE
    sta PAGE

    stz AX
    stz AY
    ldx 0
    ldy #TEXTCOLOR
!:
    lda message,x
    beq !+
    sty D
    inx
    jmp !-
!:

    stz LENGTH
    stz SKIP

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
    ldx #<readBuffer
    ldy #>readBuffer
    lda #readBufferSize
    jsr printLine
    jmp !-

updateScrollOffset:
    lda cursorY
    sec
    sbc scrollOffset
    cmp #SCROLL_LIMIT
    bcs !+
    rts
!:
    sec
    lda cursorY
    sbc #SCROLL_LIMIT
    sta scrollOffset
    and #63
    tax
    lda #CTRL_PAGE
    sta PAGE
    stz AY
    lda #CTRL_SCRY
    sta AX
    lda rowToPixelLo,x
    sta D
    lda rowToPixelHi,x
    sta D
    rts

printLine:
    jsr print
    jsr linefeed
    jmp updateScrollOffset

printChar:
    tax
    stz PAGE
    lda cursorX
    sta AX
    lda cursorY
    sta AY
    stx D
    lda #COLOR_PAGE
    sta PAGE
    lda cursorX
    sta AX
    lda cursorY
    sta AY
    lda #TEXTCOLOR
    sta D
    inc cursorX
    rts

print:
    stx ioAddress
    sty ioAddress+1
    sta ioCount
    stz PAGE
    lda cursorX
    sta AX
    lda cursorY
    sta AY
    ldy #0
!:
    cpy ioCount
    beq !+
    lda (ioAddress),y
    sta D
    iny
    jmp !-
!:
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

sinTableLo:
    .fill 256,<(256+128*sin(i*PI/128))
sinTableHi:
    .fill 256,>(256+128*sin(i*PI/128))
message:
    .text "JOFMODORE 1.0 (C) 2020-2021 Johan Fransson"
    .byte 0

rowToPixelLo:
    .fill 64,<(i*8)
rowToPixelHi:
    .fill 64,>(i*8)

* = $FFFC
    .word start
    .word irq

.label CODE=*

*=$01 virtual
.zp {
ioAddress:
    .word 0
ioCount:
    .byte 0
 }

*=$7000 "hej" virtual
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
startValue:
    .byte 0
bg:
    .byte 0
a:
    .byte 0
x:
    .byte 0
y:
    .byte 0
scrollOffset:
    .byte 0

