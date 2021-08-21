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

.const ROWS=30
.const COLS=40
.const MEMCOLS=64

.const BGCOLOR = %000110
.const TEXTCOLOR = %111111
.const MAX_LINE_LENGTH = 49

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
    jsr readline
    ldx #<readBuffer
    ldy #>readBuffer
    lda #readBufferSize
    jsr printLine
    jmp !-

printLine:
    jsr print
    sei
    jsr linefeed
    cli
    rts

print:
    sei
    stx ioAddress
    sty ioAddress+1
    sta ioCount
    stz PAGE
    stz AX
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
    cli
    rts


irq:
    stx irqX
    sty irqY
    sta irqA
    stz CTRL_REG // Save AX AY

    inc blink
    lda #COLOR_PAGE
    sta PAGE
    lda cursorX
    sta AX
    lda cursorY
    sta AY
    lda blink
    asl
    asl
    asl
    and #$80
    ora #TEXTCOLOR
    sta D

    lda #1
    sta CTRL_REG // Restore AX AY
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
oldCursorX:
    .byte 0
oldCursorY:
    .byte 0
blink:
    .byte 0
readBuffer:
    .fill MAX_LINE_LENGTH+1,0
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
scrollPtr:
    .byte 0
control:
scrollX:
    .byte 0,0
scrollY:
    .byte 0,0
