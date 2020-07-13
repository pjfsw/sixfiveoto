.cpu _65c02
.encoding "ascii"

.label VIA = $D000
.label PORTB = VIA
.label PORTA = VIA+1
.label DDRB = VIA+2
.label DDRA = VIA+3
.label SR = VIA+10
.label SPI_PORT = PORTA

.const CLOCK = $01
.const SS = $02
.const MOSI = $40
.const MISO = $80

.const A_OUTPUTS = CLOCK | SS | MOSI
.const CLOCK0_SELECT = (SS | CLOCK) ^ $ff
.const CLOCK1_SELECT = (SS) ^ $ff
.const CLOCK0_IDLE = CLOCK ^ $ff
.const WRITE_1 = MOSI
.const WRITE_0 = $00

.label TEXTPTR = $01
.label IDENTIFIER = $0200
.label COLOR = $0201
.label CHAR = $0202

* = $F000
    lda #A_OUTPUTS
    sta DDRA

    lda #CLOCK0_SELECT
    sta SPI_PORT
    spi_read_address($2800)
    jsr spi_read_byte
    sta IDENTIFIER
    lda #CLOCK0_IDLE
    sta SPI_PORT

    lda #CLOCK0_SELECT
    sta SPI_PORT
    spi_write_address(0)

    lda #<text
    sta TEXTPTR
    lda #>text
    sta TEXTPTR+1
    lda (TEXTPTR)
!:
    {
        jsr spi_transfer
        inc TEXTPTR
        bne !+
        inc TEXTPTR+1
    !:
    }
    lda (TEXTPTR)
    bne !-

    lda #CLOCK0_IDLE
    sta SPI_PORT

    lda IDENTIFIER

    jsr init4ColorSprite
    jmp *

loop:
    // Set background color
    spi_write_address($220e)
    lda COLOR
    and #$1f
    jsr spi_write_byte
    lda #0
    jsr spi_write_byte
    spi_end()
    spi_write_address($1000 + 67 * 16)
    lda CHAR
    jsr spi_write_byte
    spi_end()

    inc COLOR
    inc CHAR
!:
    lda $8000
    beq !-
    jmp loop

init4ColorSprite:
    .var img = 63

  // SPrite image data
//  gd_write(0x4000 + 256 * img);
//  for (int i = 0; i < 256; i++) {
//    SPI.transfer(i % 4);
//  }
//  gd_end();
    spi_write_address($4000 + 256*img)

    ldx #0
!:
    txa
    and #$03
    phx
    jsr spi_write_byte
    plx
    inx
    bne !-

    spi_end()

    .var x = 120
    .var y = 120

    // 4 color palette
    spi_write_address($2880)
    spi_write(0)
    spi_write(0)

    spi_write($FF)
    spi_write($7F)

    spi_write($1F)
    spi_write($00)

    spi_write($0f)
    spi_write($7f)
    spi_end()


    .var sprite = 255
    // Position
    spi_write_address($3000 + sprite * 4)
    spi_write(x & $ff)
    spi_write($80 | (x >> 8))
    spi_write(y & $ff)
    spi_write((y >> 8) | (img << 1))
    spi_end()

    rts

.macro spi_begin() {
    lda #CLOCK0_SELECT
    sta SPI_PORT
}

.macro spi_end() {
    lda #CLOCK0_IDLE
    sta SPI_PORT
}

.macro spi_write(value) {
    lda #value
    jsr spi_write_byte
}
.macro spi_write_address(address) {
    spi_begin()
    lda #>($8000 | address)
    jsr spi_write_byte
    lda #<address
    jsr spi_write_byte
}

.macro spi_read_address(address) {
    spi_begin()
    lda #>address
    jsr spi_write_byte
    lda #<address
    jsr spi_write_byte
}

spi_transfer:
    ldx #WRITE_0        // +2
    ldy #WRITE_1        // +2
    .for (var i = 0; i < 8; i++) { // $00/$00: 27  $FF/$FF: 31 cycles
        stx SPI_PORT    // +4   default MOSI = 0
        asl             // +2   shift MSB into carry, shift 0 into LSB
        bcc !+          // +2/3 if carry clear we are done (is 0)
        sty SPI_PORT    // +4   carry set, MOSI = 1
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
    ldx #WRITE_0        // +2
    ldy #WRITE_1        // +2
    .for (var i = 0; i < 8; i++) { // $00: 21  $FF: 24 cycles
        stx SPI_PORT    // +4   default MOSI = 0
        asl             // +2   shift MSB into carry, shift 0 into LSB
        bcc !+          // +2/3 if carry clear we are done (is 0)
        sty SPI_PORT    // +4   carry set, MOSI = 1
    !:
        inc SPI_PORT    // +6   raise clock
        dec SPI_PORT    // +6   clear clock
    }                   // Min: 27*8+4=172 cycles  Max: 31*8+4=196 cycles
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

text:
    .text "GAMEDUINO ASCII TEST FROM 6502!"
    .byte 0