#importonce

parseCommand:
    ldx #0
!nextChar:
    cpx readBufferSize
    bne !charsLeft+
    // End of line
    rts
!charsLeft:
    lda readBuffer,x
    cmp #' '
    bne !+
    inx
    jmp !nextChar-
!:
    cmp #'a'
    bcc !inputError+
    cmp #'{'
    bcs !inputError+
    stx inputOffset
    jmp !startParsing+

!inputError:
    ldx #<errorMsg
    ldy #>errorMsg
    lda #errorLength
    jsr printLine
    rts

!ok:
    //ldx #<okMsg
    //ldy #>okMsg
    //lda #okLength
    //jsr printLine
    ldx commandCount
    lda commandJmpLo,x
    sta jumpPointer
    lda commandJmpHi,x
    sta jumpPointer+1
    jmp (jumpPointer)

!startParsing:
    stz commandCount
!parseNext:
    ldx commandCount
    lda commandPtrLo,x
    sta ioAddress
    beq !inputError-
    lda commandPtrHi,x
    sta ioAddress+1
    ldx inputOffset
    ldy #0
!parseChar:
    lda (ioAddress),y
    bne !+
    lda #BGCOLOR
    sta SCR_BG
    jmp !ok-
!:
    cpx readBufferSize
    bcc !+
    stz SCR_BG
    jmp !inputError-
!:
    cmp readBuffer,x
    bne !nextCommand+
    iny
    inx
    jmp !parseChar-
!nextCommand:
    inc commandCount
    jmp !parseNext-


okMsg:
    .text "Ok!"
.label okLength = *-okMsg
errorMsg:
    .text "Error!"
.label errorLength = *-errorMsg

commandPtrLo:
    .byte <cmdClear, <cmdSys, 0
commandPtrHi:
    .byte >cmdClear, >cmdSys, 0
commandJmpLo:
    .byte <clearScreen, <callAddress, 0
commandJmpHi:
    .byte >clearScreen, >callAddress, 0
arguments:
    .byte 0,1,0

cmdClear:
    .text "clear"
    .byte 0
cmdSys:
    .text "sys"
    .byte 0