#importonce

#import "string.asm"

.segment DosCode

pokeByte:
    lda argumentLength+1
    ldx #<argument2
    ldy #>argument2
    jsr readHexString
    bcc !+
    jmp valueError

!:
    stx peekAddress
    sty peekAddress+1
    jsr argToHex
    bcc !+
    jmp valueError
!:
    txa
    sta (peekAddress)
    rts

peekByte:
    jsr argToHex
    bcc !+
    jmp valueError
!:
    stx ioAddress
    sty ioAddress+1
    lda (ioAddress)
    jsr printByte
    jmp linefeed

peekPage:
    jsr argToHex
    bcc !+
    jmp valueError
!:
    stz peekAddress
    sty peekAddress+1
    ldy #0
!nextByte:
    tya
    and #$07
    bne !+
    jsr printPeekAddress
!:
    lda (peekAddress),y
    phy
    jsr printByte
    lda #' '
    jsr printChar
    ply
    iny
    tya
    and #$07
    beq !printAscii+
    jmp !nextByte-

!printAscii:
    tya
    sec
    sbc #8
    tay
!nextAscii:
    lda (peekAddress),y
    phy
    jsr printChar
    ply
    iny
    tya
    and #$07
    bne !nextAscii-
    phy
    jsr linefeed
    ply
    tya
    bne !nextByte-
    rts

printPeekAddress:
    phy
    lda peekAddress+1
    jsr printByte
    pla
    pha
    jsr printByte
    lda #' '
    jsr printChar
    ply
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

.segment Zeropage

.zp {
peekAddress:
    .word 0
}