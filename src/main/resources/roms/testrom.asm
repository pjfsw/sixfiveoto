* = $F000

!:
    jsr vbl
    lda #0
    jsr paint

    jsr vbl
    lda #1
    jsr paint

    jsr vbl
    lda #2
    jsr paint

    jsr vbl
    lda #3
    jsr paint

    jsr vbl
    lda #2
    jsr paint

    jsr vbl
    lda #1
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
    sta $8000,x
    inx
    bne !-
    rts
