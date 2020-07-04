.cpu _65c02

* = $F000

.label gfxptr = $10
.label offset = $200
.label color = $12
.const WIDTH = 32
.const HEIGHT = 32
.const MIDPOINT = 16
.const SCALE = 14

loop:
    ldy #WIDTH-1
!:
    clc
    tya
    adc offset
    tax
    lda sintablelo,x
    sta gfxptr
    lda sintablehi,x
    sta gfxptr+1

    lda.z color
    sta (gfxptr),y
    dey
    bpl !-

    inc offset
vbl:
    // Vertical blank
    lda $8000
    beq vbl
    clc
    tax
    adc offset
    sta offset
    lda colortable,x
    sta.z color

    ldx #0
!:
    stz $8000,x // 65C02 test
    stz $8100,x // 65C02 test
    stz $8200,x // 65C02 test
    stz $8300,x // 65C02 test
    inx
    bne !-

    jmp loop

.align $100
sintablelo:
    .fill 256, < ($8000+(WIDTH*floor(MIDPOINT+SCALE*sin(toRadians(i*360/256)))))
sintablehi:
    .fill 256, > ($8000+(WIDTH*floor(MIDPOINT+SCALE*sin(toRadians(i*360/256)))))
colortable:
    .byte $2, $13, $ff, $ff
