.cpu _65c02
.encoding "ascii"

.const D = $c000
.const AY = $c001
.const AX = $c002
.const PAGE = $c003
.const LENGTH = $c004
.const SKIP = $c005
.const SCR_BG = $c006


.const ROWS=30
.const COLS=40
.const MEMCOLS=64

* = $E000
start:
    lda #%010001
    sta SCR_BG

    lda #39
    sta LENGTH
    lda #24
    sta SKIP

    lda #0
    sta PAGE
    jsr fill

    lda #1
    sta PAGE
    jsr fill

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



#import "../system/pins.asm"

* = $FFFC
    .word start

.label CODE=*
*=$02 "Zeropage" virtual
.zp {
}