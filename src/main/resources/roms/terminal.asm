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

.label TEXTPTR = $01

//        via.setOutput(0,0, gti.getClockIn()); // SPI Clock
//        via.setOutput(0,2, gti.getSlaveSelect()); // Slave Select
//        via.setOutput(0,6, gti.getSlaveIn()); // MOSI
//        via.setInput(0,7, gti.getSlaveOut()); // MISO
//        via.setInput(1,7, gti.getSlaveReady()); // Slave Ready
//        via.setInput(1,6, gti.getConnected())); // User connected

* = $F000
    lda #A_OUTPUTS
    sta DDRA

    jsr resetText

derpes:
    lda #CLOCK0_SELECT
    sta PORTA

    jsr nextLetter
    jsr exchangeByte

    lda #CLOCK0_IDLE
    sta PORTA

    jmp derpes

nextLetter:
    inc TEXTPTR
    bne !+
    inc TEXTPTR+1
!:
    lda.z (TEXTPTR)
    bne !+

resetText:
    lda #<(text)
    sta TEXTPTR
    lda #>(text)
    sta TEXTPTR+1
    lda.z (TEXTPTR)
!:
    rts

.align $100
text:
    .text "Hello world from 6502!"
    .byte 13,10,0
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
