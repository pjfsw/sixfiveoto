.cpu _65c02
.encoding "ascii"

.const ENABLE1 = $01
.const ENABLE2 = $02
.const RS = $04

* = $F000
start:
    lda #$ff
    sta DDRA
    lda #$ff
    sta DDRB

    ldx #0
!:
    lda msg,x
    sta PORTA

    lda #ENABLE1 | RS
    sta PORTB
    stz PORTB
    inx
    cpx #msg_len
    bne !-

    ldx #0
!:
    lda msg2,x
    sta PORTA

    lda #ENABLE2 | RS
    sta PORTB
    stz PORTB
    inx
    cpx #msg2_len
    bne !-

    jmp *

msg:
    .text "JOFMODORE 2.0"
.label msg_len = *-msg

msg2:
    .text "Type a command to begin"
.label msg2_len = *-msg2

#import "../system/pins.asm"

* = $FFFC
    .word start
