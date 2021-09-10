#importonce

// A Number of chars
// X Lo byte of string
// Y Hi byte of string
// Return: Lo number in X, Hi number in Y, Carry clear = OK
readHexString:
    stx ioAddress
    sty ioAddress+1
    cmp #5  // Only support 16-bit numbers
    bcc !+
    rts
!:
    stz currentNibble
    stz currentByte
    tay
    dey

!nextNibble:
    cpy #$ff
    bne !+
    // DONE!
    ldx tmpNumber
    ldy tmpNumber+1
    clc
    rts
!:
    lda (ioAddress),y
    jsr checkNibble
    bcc !+
    rts
!:
    tax
    lda currentNibble
    cmp #1
    beq !storeHiNibble+
    // Lower nibble, store byte
    txa
    ldx currentByte
    sta tmpNumber,x
    inc currentNibble
    dey
    jmp !nextNibble-

!storeHiNibble:
    txa
    asl
    asl
    asl
    asl
    ldx currentByte
    ora tmpNumber,x
    sta tmpNumber,x
    stz currentNibble
    inc currentByte
    dey
    jmp !nextNibble-
    rts

checkNibble:
    ldx #15
!:
    cmp digit,x
    beq !foundDigit+
    dex
    bpl !-

    ldx #15
!:
    cmp upperCaseDigit,x
    beq !foundDigit+
    dex
    bpl !-
    sec
    rts
!foundDigit:
    txa
    clc
    rts

digit:
    .text "0123456789abcdef"
upperCaseDigit:
    .text "0123456789ABCDEF"
