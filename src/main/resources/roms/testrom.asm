* = $F000

.label offset = $200


loop:
    inc offset
    ldx #0
!:
    clc
    txa
    adc offset
    tay
    txa
    adc sintable,y
    tay
    lda offset
    sta $8000,y
    inx
    cpx #16
    bcc !-

    inc offset
vbl:
    // Vertical blank
    lda $8000
    beq vbl

    lda #0
    ldx #0
!:
    sta $8000,x
    inx
    bne !-

    jmp loop

.align $100
sintable:
    .fill 256, 16*round(7.5+7.5*sin(toRadians(i*360/64)))
