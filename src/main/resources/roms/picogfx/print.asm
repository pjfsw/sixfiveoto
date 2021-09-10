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
    lda #1
    sta cursorX
    jsr print
    jmp linefeed

printChar:
    tax
    jsr setPosition
    stx D
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
    jsr setPosition
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
    jsr setPosition
    lda #' '
    ldx #64
!:
    sta D
    dex
    bne !-

    jmp updateScrollOffset

setPosition:
    clc
    ldx cursorY
    lda yToScreenLo,x
    adc cursorX
    sta AL
    adc yToScreenHi,x
    sta AH
    rts

setColorPosition:
    clc
    ldx cursorY
    lda yToColorLo,x
    adc cursorX
    sta AL
    adc yToColorHi,x
    sta AH
    rts

yToScreenLo:
    .fill 64,<(i<<6)
yToScreenHi:
    .fill 64,>(i<<6)

yToColorLo:
    .fill 64,<($1000+(i<<6))
yToColorHi:
    .fill 64,>($1000+(i<<6))