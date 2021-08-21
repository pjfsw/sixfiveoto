#importonce
#import "keyboard.asm"

readline:
    jsr clearBuffer
nextKey:
    lda cursorX
    sta oldCursorX
    lda cursorY
    sta oldCursorY

    jsr readKeyboard
    cmp #0
    beq nextKey
    cmp #KEY_ENTER
    bne !+
    sei
    jsr linefeed
    jsr restoreCursor
    cli
    rts
!:
    ldx readBufferSize
    cpx #MAX_LINE_LENGTH // Stop reading after 63 chars
    bcs nextKey

    sta readBuffer,x
    lda cursorX
    sta AX
    lda cursorY
    sta AY
    stz PAGE
    lda readBuffer,x
    sta D
    inx
    stx readBufferSize

    sei
    jsr moveCursor
    jsr restoreCursor
    cli
    jmp nextKey

clearBuffer:
    stz readBufferSize
    ldx #63
!:
    stz readBuffer,x
    dex
    bpl !-
    rts

restoreCursor:
    lda #COLOR_PAGE
    sta PAGE
    lda oldCursorX
    sta AX
    lda oldCursorY
    sta AY
    lda #TEXTCOLOR
    sta D
    rts

moveCursor:
    lda cursorX
    clc
    adc #1
    cmp #50
    bcc !+
    jmp linefeed
!:
    sta cursorX
    rts

linefeed:
    stz cursorX
    clc
    lda cursorY
    adc #1
    and #$1f
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
