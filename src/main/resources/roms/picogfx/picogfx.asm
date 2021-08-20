.cpu _65c02
.encoding "ascii"

.const D = $c000
.const AY = $c001
.const AX = $c002
.const PAGE = $c003
.const LENGTH = $c004
.const SKIP = $c005
.const SCR_BG = $c006
.const SCREEN_PAGE = 0
.const COLOR_PAGE = 1
.const CTRL_PAGE = 2

.const CTRL_SCRX = 0

.const ROWS=30
.const COLS=40
.const MEMCOLS=64

.const BGCOLOR = %000110
.const TEXTCOLOR = %111111

.const SPI_VIA = $c800
.const SS_PORT = SPI_VIA
.const SPI_PORT = SPI_VIA+1
.const SS_DDR = SPI_VIA+2
.const SPI_DDR = SPI_VIA+3

* = $E000
start:
    sei
    jsr setupPorts

    lda #BGCOLOR
    sta SCR_BG

    lda #39
    sta LENGTH
    lda #24
    sta SKIP

    lda #SCREEN_PAGE
    sta PAGE
    stz startValue
    jsr fill

    stz AX
    stz AY
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
    lda #255-75
    sta startValue

    jsr fill

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

!:
    jsr readKeyboard
    cmp #0
    beq !-

    sta input
    stz AX
    stz AY
    stz PAGE
    lda input
    sta D

    jmp !-


fill:
    stz AX
    ldy #2
    sty AY
    ldy startValue
    lda #0
!:  {
        ldx #255
    !:
        sty D
        iny
        dex
        bne !-
    }
    clc
    adc #1
    cmp #8
    bcc !-
    rts

irq:
    stx irqX
    sty irqY
    sta irqA

    lda #CTRL_PAGE
    sta PAGE
    stz AX
    stz AY
    ldx #0
    {
    !:
        lda control,x
        sta D
        inx
        cpx #4
        bcc !-
    }
    /*inc scrollPtr
    ldx scrollPtr
    lda sinTableLo,x
    sta scrollY
    lda sinTableHi,x
    sta scrollY+1*/

    stz AX
    stz AY
    stz PAGE
    lda input
    sta D

    //jsr checkInput
    ldx irqX
    ldy irqY
    lda irqA
    rti

checkInput:
    lda $d000
    rol
    rol
    bcs !+
    {
        clc
        lda scrollX
        adc #2
        bcc !+
        inc scrollX+1
    !:
        sta scrollX
        rts
    }
!:
    rol
    bcs !+
    {
        sec
        lda scrollX
        sbc #2
        bcs !+
        dec scrollX+1
    !:
        sta scrollX
        rts
    }
!:
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

#import "keyboard.asm"

sinTableLo:
    .fill 256,<(256+128*sin(i*PI/128))
sinTableHi:
    .fill 256,>(256+128*sin(i*PI/128))
message:
    .text "Hello world!"
    .byte 0
* = $FFFC
    .word start
    .word irq

.label CODE=*
*=$7000 "hej" virtual
irqX:
    .byte 0
irqY:
    .byte 0
irqA:
    .byte 0
input:
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
