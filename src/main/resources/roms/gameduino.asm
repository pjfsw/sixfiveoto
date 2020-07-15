.cpu _65c02
.encoding "ascii"

.label VIA = $D000
.label PORTB = VIA
.label PORTA = VIA+1
.label DDRB = VIA+2
.label DDRA = VIA+3
.label SR = VIA+10

.label SS_PORT = PORTB
.const GD_SS = 1 << 1
.const CART_SS = 1 << 2

.label SPI_PORT = PORTA
.const CLOCK = $01
.const MOSI = 1 << 6
.const MISO = 1 << 7

.const A_OUTPUTS = CLOCK | MOSI
.const B_OUTPUTS = GD_SS | CART_SS

.const GD_SELECT = GD_SS ^ $ff
.const CART_SELECT = CART_SS ^ $ff
.const IDLE = $ff

.const WRITE_1 = MOSI
.const WRITE_0 = $00

.label TEXTPTR = $01
.label IDENTIFIER = $0200
.label FRAMES = $0201
.label CHAR = $0202

.label relPosition = 3
.label position = 2
.const SPRITEIMG = 63

.label loadBuffer = $0300
.label loadPosition = 3

* = $F000
    lda #A_OUTPUTS
    sta DDRA
    lda #B_OUTPUTS
    sta DDRB

    gd_read_address($2800)
    jsr spi_read_byte
    sta IDENTIFIER
    spi_end()

    gd_write_address(0)

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

    spi_end()

    //jsr load_data

    jsr init4ColorSprite

fetdemo:
    .var spritesToDraw = 49
    .var y = 100

    gd_write_address($3000 + (255-spritesToDraw) * 4)

    ldx #spritesToDraw // Draw 34 sprites
!:
    phx
    txa
    asl
    clc
    adc position
    sta relPosition
    tax
    lda costable,x
    jsr spi_write_byte
    spi_write($80 | (5<<1))
    ldx relPosition
    lda sintable,x
    jsr spi_write_byte
    spi_write((y >> 8) | (SPRITEIMG << 1))

    plx
    dex
    bne !-

    spi_end()

!:
    lda $8000
    beq !-

    sta FRAMES
    clc
    adc position
    sta position
    // Set background color
    gd_write_address($280e)
    ldx FRAMES
    lda framerateColors,x
    and #$1f
    jsr spi_write_byte
    spi_end()

    jmp fetdemo

load_data:
    stz loadPosition
!:
    cart_read()
    {
        lda #0
        jsr spi_write_byte

        lda loadPosition
        jsr spi_write_byte

        lda #0
        jsr spi_write_byte

        ldx #0
    !:
        phx
        jsr spi_read_byte
        plx

        sta loadBuffer,x

        inx
        bne !-
    }
    spi_end()

    gd_begin()
    {
        lda loadPosition
        and #$0f
        ora #$80
        jsr spi_write_byte
        spi_write($00)
        ldx #0
    !:
        lda loadBuffer,x
        phx
        jsr spi_write_byte
        plx
        inx
        bne !-
    }
    spi_end()

    inc loadPosition
    lda loadPosition
    cmp #16
    bne !-

    rts

init4ColorSprite:

  // SPrite image data
//  gd_write(0x4000 + 256 * img);
//  for (int i = 0; i < 256; i++) {
//    SPI.transfer(i % 4);
//  }
//  spi_end();
    gd_write_address($4000 + 256*SPRITEIMG)

    ldx #0
!:
    //txa
    //and #$03
    phx
    lda spriteData,x
    jsr spi_write_byte
    plx
    inx
    bne !-

    spi_end()

    // 4 color palette
    gd_write_address($2880)
    spi_write(0)
    spi_write($80)

    spi_write($FF)
    spi_write($7F)

    spi_write($1F)
    spi_write($00)

    spi_write($0f)
    spi_write($7f)
    spi_end()

    rts

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

framerateColors:
    .byte 0, 0, 15, 23, 31

.align $100
costable:
    .fill 256, round(127.5+127.5*cos(toRadians(i*360/256)))

sintable:
    .fill 256, round(127.5+127.5*sin(toRadians(i*360/128)))

spriteData:
      .byte 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0
      .byte 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0
      .byte 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2
      .byte 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2
      .byte 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2
      .byte 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2
      .byte 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0
      .byte 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0

      .byte 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2
      .byte 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2
      .byte 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2
      .byte 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2
      .byte 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2
      .byte 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2
      .byte 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2
      .byte 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2

//    .byte 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0
//    .byte 0, 0, 0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0
//    .byte 0, 0, 1, 2, 3, 3, 3, 3, 3, 3, 3, 3, 2, 1, 0, 0
//    .byte 0, 1, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 1, 0
//    .byte 1, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 1
//    .byte 1, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 1
//    .byte 1, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 1
//    .byte 1, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 1
//
//    .byte 1, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 1
//    .byte 1, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 1
//    .byte 1, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 1
//    .byte 1, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 1
//    .byte 3, 1, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 1, 0
//    .byte 3, 3, 1, 2, 3, 3, 3, 3, 3, 3, 3, 3, 2, 1, 0, 0
//    .byte 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0
//    .byte 3, 3, 3, 3, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0
