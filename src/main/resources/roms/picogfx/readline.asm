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
    cmp #KEY_BACKSPACE
    bne !+
    jsr restoreCursor
    ldx readBufferSize
    beq nextKey
    dex
    stx readBufferSize
    //stz readBuffer,x
    dec cursorX
    stz PAGE
    lda cursorX
    sta AX
    lda cursorY
    sta AY
    lda #' '
    sta D
    jmp nextKey
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
    lda textColor
    sta D
    rts

moveCursor:
    inc cursorX
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
    ora textColor
    sta D
    rts