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
    jsr setPosition
    lda #' '
    sta D
    jmp nextKey
!:
    ldx readBufferSize
    cpx #MAX_LINE_LENGTH // Stop reading after 63 chars
    bcs nextKey

    sta readBuffer,x

    jsr restoreCursor
    jsr setPosition
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
    jsr setColorPosition
    stz D
    rts

moveCursor:
    inc cursorX
    rts


updateCursor:
    jsr setColorPosition
    lda #1
    sta D
    rts