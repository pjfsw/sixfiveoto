* = $F000

!:
    jsr vbl
    jsr paint
    jmp !-

vbl:
    // Vertical blank
    lda $8000
    beq vbl
    rts

paint:
    ldx #0
!:
    txa
    sta $8000,x
    inx
    bne !-
    rts
