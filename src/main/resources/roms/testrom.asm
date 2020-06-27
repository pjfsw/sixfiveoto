* = $F000

!:
    lda #0
{
    ldx #$FA
!:
    sta $0200,x
    inx
    bne !-
}

    lda #1
{
    ldx #$FA
!:
    sta $0200,x
    inx
    bne !-
}
    jmp !-
