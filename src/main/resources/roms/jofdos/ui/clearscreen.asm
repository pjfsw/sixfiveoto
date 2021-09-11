#importonce

.segment DosCode

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
    jsr fill8192
    lda #<pico_screen_select
    sta AL
    lda #>pico_screen_select
    sta AH
    stz D
    lda #<pico_bitmap_start
    sta AL
    stz D
    stz D
    stz D

    stz cursorX
    stz cursorY
    lda #<pico_scroll_y
    sta AL
    lda #>pico_scroll_y
    sta AH
    stz D
    stz D
    stz scrollOffset
    rts

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

