#importonce

#import "picoreg.asm"

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
    lda #<pico_scroll_y
    sta AL
    lda #>pico_scroll_y
    sta AH
    lda rowToPixelLo,x
    sta D
    lda rowToPixelHi,x
    sta D
    rts

printLine:
    jsr print
    jmp linefeed

printChar:
    pha
    jsr setPosition
    pla
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
    pha
    jsr setPosition
    pla
    clc
    adc cursorX
    sta cursorX
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

fillLine:
    ldx #64
!:
    sta D
    dex
    bne !-
    rts

linefeed:
    stz cursorX
    clc
    lda cursorY
    adc #1
    and #63
    sta cursorY
    jsr setPosition
    lda #' '
    jsr fillLine

    jsr setColorPosition
    lda #0
    jsr fillLine
    jmp updateScrollOffset

.macro setVramPosition(lowTable, highTable) {
    ldx cursorY
    clc
    lda lowTable,x
    adc cursorX
    sta AL
    lda #0
    adc highTable,x
    sta AH
}

setPosition:
    setVramPosition(yToScreenLo, yToScreenHi)
    rts

setColorPosition:
    setVramPosition(yToColorLo, yToColorHi)
    rts

yToScreenLo:
    .fill 64,<(i<<6)
yToScreenHi:
    .fill 64,>(i<<6)

yToColorLo:
    .fill 64,<($1000+(i<<6))
yToColorHi:
    .fill 64,>($1000+(i<<6))