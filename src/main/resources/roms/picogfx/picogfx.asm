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

* = $E000
start:
    sei
    lda #%010001
    sta SCR_BG

    lda #39
    sta LENGTH
    lda #24
    sta SKIP

    lda #SCREEN_PAGE
    sta PAGE
    jsr fill

    lda #COLOR_PAGE
    sta PAGE
    jsr fill

    stz LENGTH
    stz SKIP

    lda #CTRL_PAGE
    sta PAGE

    cli
    jmp *

fill:
    stz AX
    stz AY
    ldy #0
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
    cmp #5
    bcc !-
    rts

irq:
    stx x
    sty y
    sta a

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
    inc scrollPtr
    ldx scrollPtr
//    lda cosTableLo,x
//    sta scrollX
//    lda cosTableHi,x
//    sta scrollX+1
    lda sinTableLo,x
    sta scrollY
    lda sinTableHi,x
    sta scrollY+1

    jsr checkInput
    ldx x
    ldy y
    lda a
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

#import "../system/pins.asm"

cosTableLo:
    .fill 256,<(256+255*cos(i*PI/128))
cosTableHi:
    .fill 256,>(256+255*cos(i*PI/128))

sinTableLo:
    .fill 256,<(256+255*sin(i*PI/128))
sinTableHi:
    .fill 256,>(256+255*sin(i*PI/128))

* = $FFFC
    .word start
    .word irq

.label CODE=*
*=$7000 "hej" virtual
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
