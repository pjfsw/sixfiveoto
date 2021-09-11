#importonce

#import "loader.asm"
#import "../ui/print.asm"

.segment DosCode

compareString:
    tax
!nextChar:
    cpx #0
    beq !endOfSource+
    lda (stringTarget)
    cmp (stringSource)
    bne !mismatch+
    inc stringTarget
    bne !+
    inc stringTarget+1
!:
    inc stringSource
    bne !+
    inc stringSource+1
!:
    dex
    jmp !nextChar-

!endOfSource:
    clc
    lda (stringTarget)
    bne !+
    sec
!:
    rts
!mismatch:
    clc
    rts

load:
    jsr load_dir
    lda #<LOAD_TARGET
    sta loadTarget
    lda #>LOAD_TARGET
    sta loadTarget+1
!:
    lda loadTarget
    clc
    adc #1
    sta stringTarget
    lda loadTarget+1
    sta stringTarget+1
    lda #<argument1
    sta stringSource
    lda #>argument1
    sta stringSource+1
    lda argumentLength
    jsr compareString
    bcs !+
    lda loadTarget
    clc
    adc #$10
    sta loadTarget
    bne !-
    ldx #<notFoundMessage
    ldy #>notFoundMessage
    lda #notFoundLength
    jsr printLine
    rts
!:
    lda #>LOAD_TARGET
    sta loadTarget+1
    lda #2
    sta loadSource
    stz loadSource+1
    lda #124
    sta loadCount
    jsr load_data
    rts

notFoundMessage:
    .text "Not found!"
.label notFoundLength = *-notFoundMessage

.segment Zeropage

.zp {
stringSource:
    .word 0
stringTarget:
    .word 0
}