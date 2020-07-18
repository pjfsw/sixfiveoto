#importonce

.cpu _65c02
.encoding "ascii"

#import "pins.asm"

//
// NOTE!!
//
// A and X registers are affected.
// Y register is guaranteed to be left alone.
//
.macro gd_begin() {
    lda #GD_SELECT
    sta SS_PORT
}

.macro gd_write_address(address) {
    gd_begin()
    lda #>($8000 | address)
    jsr spi_write_byte
    lda #<address
    jsr spi_write_byte
}

.macro gd_read_address(address) {
    gd_begin()
    lda #>address
    jsr spi_write_byte
    lda #<address
    jsr spi_write_byte
}

.macro cart_begin() {
    lda #CART_SELECT
    sta SS_PORT
}

.macro cart_read() {
    cart_begin()
    lda #$03    // Read cmd
    jsr spi_write_byte
}

.macro cart_read_address(address) {
    cart_read()
    lda #address >> 16
    jsr spi_write_byte
    lda #(address >> 8) & $ff
    jsr spi_write_byte
    lda #address & $ff
    jsr spi_write_byte
}

.macro spi_end() {
    lda #IDLE
    sta SS_PORT
}

.macro spi_write(value) {
    lda #value
    jsr spi_write_byte
}


spi_transfer:
    ldx #WRITE_1        // +2
    .for (var i = 0; i < 8; i++) { // $00/$00: 27  $FF/$FF: 31 cycles
        stz SPI_PORT    // +4   default MOSI = 0
        asl             // +2   shift MSB into carry, shift 0 into LSB
        bcc !+          // +2/3 if carry clear we are done (is 0)
        stx SPI_PORT    // +4   carry set, MOSI = 1
    !:
        inc SPI_PORT    // +6   raise clock
        dec SPI_PORT    // +6   clear clock
        bit SPI_PORT    // +3
        bpl !+          // +2/3 input=0, skip
        ora #$01        // +2   set LSB=1
    !:
    }                   // 27*8+4=220   31*8+4=252
    rts

spi_write_byte:
    ldx #WRITE_1        // +2
    .for (var i = 0; i < 8; i++) { // $00: 21  $FF: 24 cycles
        stz SPI_PORT    // +4   default MOSI = 0
        asl             // +2   shift MSB into carry, shift 0 into LSB
        bcc !+          // +2/3 if carry clear we are done (is 0)
        stx SPI_PORT    // +4   carry set, MOSI = 1
    !:
        inc SPI_PORT    // +6   raise clock
        dec SPI_PORT    // +6   clear clock
    }                   // Min: 27*8+4=172 cycles  Max: 31*8+4=196 cycles
    rts

spi_write_zero:
    ldx #1              // +2
    .for (var i = 0; i < 8; i++) { // 64 cycles
        stx SPI_PORT    // +4
        stz SPI_PORT    // +4
    }                   // 66 cycles
    rts

spi_read_byte:
    ldx #$7F            // +2
    .for (var i = 0;i < 8; i++) { // 18 cycles
        cpx SPI_PORT    // +4
        rol             // +2
        inc SPI_PORT    // +6
        dec SPI_PORT    // +6
    }
    eor #$ff            // 2  = 148 cycles
    rts



//spi_write_byte_sr:      // Write byte to SPI using shift register
//    ldx #CLOCK0_SELECT  // +2
//    ldy #CLOCK1_SELECT  // +2
//    sta SR              // +4
//    .for (var i = 0; i < 8; i++) { // 8 cycles
//        sty SPI_PORT    // +4
//        stx SPI_PORT    // +4
//    }                   // 8*8+8 = 72 cycles
//    rts
//
