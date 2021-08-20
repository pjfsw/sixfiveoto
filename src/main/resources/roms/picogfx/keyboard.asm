#importonce

.const CLOCK = $01
.const KEYBOARD_SELECT = $FE
.const NO_SELECT = $FF

readSpiByte:
    ldy #1              // +2
    ldx #$7F            // +2
    .for (var i = 0; i < 8; i++) { // 14 cycles
        sty SPI_PORT    // +4
        cpx SPI_PORT    // +4
        rol             // +2
        stz SPI_PORT    // +4
    }
    eor #$ff            // +2
    rts

readKeyboard:
    ldx #KEYBOARD_SELECT
    stx SS_PORT

    jsr readSpiByte

    ldx #NO_SELECT
    stx SS_PORT
    rts

