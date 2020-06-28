* = $F000

!:
    iny
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
    tya
    sta $8000,x
    inx
    bne !-
    rts
