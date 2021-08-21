#importonce
#import "keyboard.asm"

readline:
    jsr clearBuffer
nextKey:
    jsr updateCursor
    jsr readKeyboard
    cmp #0
    beq nextKey
    cmp #KEY_ENTER
    bne !+
    jsr restoreCursor
    jsr linefeed
    rts
!:
    ldx readBufferSize
    cpx #MAX_LINE_LENGTH // Stop reading after 63 chars
    bcs nextKey

    sta readBuffer,x

    jsr restoreCursor
    lda cursorX
    sta AX
    lda cursorY
    sta AY
    stz PAGE
    lda readBuffer,x
    sta D
    inx
    stx readBufferSize

    jsr moveCursor
    jmp nextKey

clearBuffer:
    stz readBufferSize
    ldx #64
!:
    stz readBuffer,x
    dex
    bpl !-
    rts

restoreCursor:
    lda #COLOR_PAGE
    sta PAGE
    lda cursorX
    sta AX
    lda cursorY
    sta AY
    lda #TEXTCOLOR
    sta D
    rts

moveCursor:
    inc cursorX
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
    rts

updateCursor:
    lda #COLOR_PAGE
    sta PAGE
    lda cursorX
    sta AX
    lda cursorY
    sta AY
    lda cursorBlink
    asl
    asl
    asl
    and #$80
    ora #TEXTCOLOR
    sta D
    rts