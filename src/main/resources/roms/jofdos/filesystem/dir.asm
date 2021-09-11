#importonce

#import "loadtarget.asm"
#import "../ui/print.asm"

.segment DosCode

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

    lda #<(LOAD_TARGET+1)
    sta loadSource
    lda #>LOAD_TARGET
    sta loadSource+1
!:
    {
        lda (loadSource)
        bne !+
        rts
    !:
    }
    ldx loadSource
    ldy loadSource+1
    lda #14
    jsr printLine

    clc
    lda loadSource
    adc #$10
    sta loadSource
    bcc !-
    rts
