#importonce

updateScrollOffset:
    lda cursorY
    sec
    sbc scrollOffset
    cmp #SCROLL_LIMIT
    bcs !+
    rts
!:
    sec
    lda cursorY
    sbc #SCROLL_LIMIT
    sta scrollOffset
    and #63
    tax
    lda #CTRL_PAGE
    sta PAGE
    stz AY
    lda #CTRL_SCRY
    sta AX
    lda rowToPixelLo,x
    sta D
    lda rowToPixelHi,x
    sta D
    rts

printLine:
    jsr print
    jmp linefeed

printChar:
    tax
    stz PAGE
    lda cursorX
    sta AX
    lda cursorY
    sta AY
    stx D
    lda #COLOR_PAGE
    sta PAGE
    lda cursorX
    sta AX
    lda cursorY
    sta AY
    lda #TEXTCOLOR
    sta D
    inc cursorX
    rts

printByte:
    tay
    lsr
    lsr
    lsr
    lsr
    and #15
    tax
    lda digit,x
    jsr printChar
    tya
    and #15
    tax
    lda digit,x
    jsr printChar
    rts

print:
    stx ioAddress
    sty ioAddress+1
    sta ioCount
    stz PAGE
    lda cursorX
    sta AX
    lda cursorY
    sta AY
    ldy #0
!:
    cpy ioCount
    beq !+
    lda (ioAddress),y
    sta D
    iny
    jmp !-
!:
    rts

linefeed:
    stz cursorX
    clc
    lda cursorY
    adc #1
    sta cursorY
    sta AY
    stz AX
    stz PAGE
    lda ' '
    ldx #64
!:
    sta D
    dex
    bne !-

    lda cursorY
    sta AY
    stz AX
    lda #COLOR_PAGE
    sta PAGE
    lda #TEXTCOLOR
    ldx #64
!:
    sta D
    dex
    bne!-
    jmp updateScrollOffset
