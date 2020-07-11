.cpu _65c02
.encoding "ascii"

.label VIA = $D000
.label PORTB = VIA
.label PORTA = VIA+1
.label DDRB = VIA+2
.label DDRA = VIA+3

.const CLOCK = $01
.const SS = $04
.const MOSI = $40

.const A_OUTPUTS = CLOCK | SS |MOSI
.const CLOCK0_SELECT = (SS | CLOCK) ^ $ff
.const CLOCK0_IDLE = CLOCK ^ $ff
.const WRITE_1 = $40
.const WRITE_0 = $00

.label INPUT = $0200
.label INPUTEND = INPUT + 20
.label TEXTPTR = $01
.label INPUTPTR = $03


//        via.setOutput(0,0, gti.getClockIn()); // SPI Clock
//        via.setOutput(0,2, gti.getSlaveSelect()); // Slave Select
//        via.setOutput(0,6, gti.getSlaveIn()); // MOSI
//        via.setInput(0,7, gti.getSlaveOut()); // MISO
//        via.setInput(1,7, gti.getSlaveReady()); // Slave Ready
//        via.setInput(1,6, gti.getConnected())); // User connected

* = $F000
    lda #A_OUTPUTS
    sta DDRA

!:
    jsr readInput

    lda #<text
    sta TEXTPTR
    lda #>text
    sta TEXTPTR+1

    jsr writeText

    lda #<INPUT
    sta TEXTPTR
    lda #>INPUT
    sta TEXTPTR+1
    jsr writeText

    lda #CLOCK0_SELECT
    sta PORTA
    lda #10
    jsr exchangeByte
    lda #13
    jsr exchangeByte
    lda #CLOCK0_IDLE
    sta PORTA


    jmp !-

writeText:
    lda #CLOCK0_SELECT
    sta PORTA

!:
    lda.z (TEXTPTR)
    beq !+
    jsr exchangeByte

    inc TEXTPTR
    bne !-
    inc TEXTPTR+1
    jmp !-
!:
    lda #CLOCK0_IDLE
    sta PORTA
    rts

readInput:
    lda #<INPUT
    sta INPUTPTR
    lda #>INPUT
    sta INPUTPTR+1

    lda #CLOCK0_SELECT
    sta PORTA

    lda #'?'
!:
    jsr exchangeByte

    cmp #13
    beq !+

    // Only store ASCII >= 32
    {
        cmp #32
        bcc !+

        jsr storeCharacter
    !:
    }

    lda #0

    ldx #<INPUTEND
    cpx INPUTPTR
    bne !-
    ldx #>INPUTEND
    cpx INPUTPTR+1
    bne !-

!:
    lda #CLOCK0_IDLE
    sta PORTA

    lda #0
    jsr storeCharacter
    rts

storeCharacter:
    sta (INPUTPTR)
    inc INPUTPTR
    bne !+
    inc INPUTPTR+1
!:
    rts

.align $100
text:
    .text "Your message is: "
    .byte 0
// * CPU flow:
// * Clock = 0
// * Slave select = 0
// * Wait Slave Ready = 0
// * Write bit
// * Clock = 1
// * Wait Slave Ready = 1
// * Read bit
// * .. repeat 7 times
// *

exchangeByte:
    ldx #WRITE_0
    ldy #WRITE_1

    clc
    .for (var i = 0; i <8 ;i++) {
    !:
        bit PORTB       // Wait slave ready  = 0
        bmi !-

        stx PORTA       // Default write 0

        rol
        bcc !+
        sty PORTA       // Shifted in carry from accumulator, store 1
    !:
        inc PORTA       // Set clock = 1

    !:                  // Wait slave ready = 1
        bit PORTB
        bpl !-

        and #$FE

        bit PORTA
        bpl !+

        ora #$1
     !:
        dec PORTA       // Set clock = 0
    }

    rts






//   .macro @SPI_writeAccumulator() {
//        .for (var i = 0; i < 8; i++) {
//            sty SPI_PORT    // default MOSI = 0
//            rol             // shift MSB into carry
//            bcc !+          // if carry clear we are done (is 0)
//            stx SPI_PORT    // carry set, MOSI = 1
//        !:
//            inc SPI_PORT    // raise clock
//            dec SPI_PORT    // clear clock
//        }
//    }
//
