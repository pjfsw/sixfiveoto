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

.const SPRITEIMG = 63



* = $0001 virtual
.zp {
relPosition:
    .byte 0
position:
    .byte 0
vb:
    .byte 0
}

* = $0200 virtual
loadPosition:
    .byte 0
identifier:
    .byte 0
frames:
    .byte 0
lastFrame:
    .byte 0
scrollX:
    .byte 0,0
scrollY:
    .byte 0,0
* = $0300 virtual
loadBuffer:
    .fill 256,0

* = $F000
    lda #A_OUTPUTS
    sta DDRA
    lda #B_OUTPUTS
    sta DDRB

    jsr load_data

    jsr init4ColorSprite

fetdemo:
    jsr scroll

    .var spritesToDraw = 42

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
.var rotation = 3
    spi_write($80 | (rotation<<1))
    ldx relPosition
    lda sintable,x
    jsr spi_write_byte
    spi_write(SPRITEIMG << 1)

    plx
    dex
    bne !-

    spi_end()

    // Wait for VBL
    //jsr wait_vertical_blank
    // End VBL

!:
    gd_read_address($2802)
    {
        jsr spi_read_byte
        tax
        sec
        sbc lastFrame
        sta frames
        stx lastFrame

    }
    spi_end()

    lda frames
    beq !-

    clc
    adc position
    sta position

    clc
    lda frames
    adc scrollX
    sta scrollX
    bcc !+
    inc scrollX+1
!:

    // Set background color
    gd_write_address($280e)
    ldx frames
    lda framerateColors,x
    and #$1f
    jsr spi_write_byte
    spi_end()

    jmp fetdemo

scroll:
    gd_write_address($2804)
    {
        lda scrollX
        jsr spi_write_byte
        lda scrollX+1
        jsr spi_write_byte
        ldx scrollX
        lda scrollSinLo,x
        jsr spi_write_byte
        ldx scrollX
        lda scrollSinHi,x
        jsr spi_write_byte
    }
    spi_end()
    rts

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

    spi_write($FF)
    spi_write($1F)

    spi_write($0f)
    spi_write($7f)
    spi_end()

    rts

#import "spi.asm"


framerateColors:
    .byte 0, 0, $0f, $1f, $ff

.align $100
costable:
    .fill 256, round(127.5+127.5*cos(toRadians(i*360/256)))

sintable:
    .fill 256, round(127.5+127.5*sin(toRadians(i*360/128)))

scrollSinLo:
    .fill 256, <(round(149.5+149.5*sin(toRadians(i*360/256))))
scrollSinHi:
    .fill 256, >(round(149.5+149.5*sin(toRadians(i*360/256))))

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
