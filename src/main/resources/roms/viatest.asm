.cpu _65c02

.label VIA = $D000
.label PORTB = VIA
.label PORTA = VIA+1
.label DDRB = VIA+2
.label DDRA = VIA+3

* = $F000

    lda #$AA
    sta DDRB

    lda #$FF
    sta DDRA

    ldx #0
    ldy #0
derpes:
    {
        lda PORTB
        and #1
        beq !+

        ldx #0
        ldy #0
    !:
        stx PORTA
        sty PORTB

    //vbl:
        // Vertical blank
      //  lda $8000
        //beq vbl

    }

    inx
    dey
    jmp derpes

