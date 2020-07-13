.cpu _65c02
.encoding "ascii"

.label VIA = $D000
.label PORTB = VIA
.label PORTA = VIA+1
.label DDRB = VIA+2
.label DDRA = VIA+3
.label SPI_PORT = PORTA

.const CLOCK = $01
.const SS = $02
.const MOSI = $40
.const MISO = $80

.const A_OUTPUTS = CLOCK | SS | MOSI
.const CLOCK0_SELECT = (SS | CLOCK) ^ $ff
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
    jsr spi_transfer
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

loop:
    // Set background color
    spi_begin()
    spi_write_address($220e)
    lda COLOR
    and #$1f
    jsr spi_write_byte
    lda #0
    jsr spi_write_byte
    spi_end()
    spi_begin()
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

.macro spi_begin() {
    lda #CLOCK0_SELECT
    sta SPI_PORT
}

.macro spi_end() {
    lda #CLOCK0_IDLE
    sta SPI_PORT
}
.macro spi_write_address(address) {
    lda #>($8000 | address)
    jsr spi_write_byte
    lda #<address
    jsr spi_write_byte
}

.macro spi_read_address(address) {
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
    }                   // 27*8+4=172   31*8+4=196
    rts


text:
    .text "GAMEDUINO ASCII TEST FROM 6502!"
    .byte 0