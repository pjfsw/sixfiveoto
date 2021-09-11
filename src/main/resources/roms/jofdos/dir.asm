#importonce

#import "loadtarget.asm"
#import "print.asm"

load_dir:
    stz loadSource
    stz loadSource+1
    lda #1
    sta loadCount
    stz loadTarget
    lda #>LOAD_TARGET
    sta loadTarget+1
    jsr load_data
    rts

dir:
    jsr load_dir

    lda #LOAD_TARGET
    sta loadSource
    lda #>LOAD_TARGET
    sta loadSource+1
!:
    {
        lda (loadSource)
        beq !+
        ldx loadSource
        ldy loadSource+1
        lda #12
        jsr print

        ldy #14
        lda (loadSource),y
        jsr printByte
        lda #'0'
        jsr printChar
        lda #'0'
        jsr printChar
        jsr linefeed
    !:
    }
    clc
    lda loadSource
    adc #$10
    sta loadSource
    bcc !-
    rts
