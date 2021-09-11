#importonce

fill8192:
    ldx #32     // Clean 8192 bytes of VRAM = palette 0
!:
    ldy #0
    {
    !:
        stz D
        dey
        bne !-
    }
    dex
    bne !-
    rts

clearScreen:
    lda #<pico_scr_0
    sta AL
    lda #>pico_scr_0
    sta AH
    jsr fill8192
    lda #<pico_col_0
    sta AL
    lda #>pico_col_0
    sta AH
    jmp fill8192