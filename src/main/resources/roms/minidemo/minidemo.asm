.cpu _65c02
.encoding "ascii"

#import "../system/load_address.asm"
#import "../system/pins.asm"

.const SPRITEIMG = 63

.pseudopc LOAD_ADDRESS {
    .print >(prgEnd-*)
    .byte >(prgEnd-*)

    jsr init4ColorSprite
    jsr drawlotsOfText

fetdemo:
    jsr scroll

    .var spritesToDraw = 41

    gd_write_address($3000 + (255-spritesToDraw) * 4)

    ldy #spritesToDraw // Draw 34 sprites
!:
    tya
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

    dey
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

drawlotsOfText:
    gd_write_address(0)
    {
        lda #16
        sta text_counter
    !:
        {
            ldy #0
        !:
            lda message,y
            jsr spi_write_byte
            iny
            bne !-
        }
        dec text_counter
        bne !-
    }
    spi_end()
    rts

scroll:
    gd_write_address($2804)
    {
        lda scrollX
        jsr spi_write_byte
        lda scrollX+1
        jsr spi_write_byte
        ldy scrollX
        lda scrollSinLo,y
        jsr spi_write_byte
        lda scrollSinHi,y
        jsr spi_write_byte
    }
    spi_end()
    rts

init4ColorSprite:

  // SPrite image data
//  gd_write(0x4000 + 256 * img);
//  for (int i = 0; i < 256; i++) {
//    SPI.transfer(i % 4);
//  }
//  spi_end();
    gd_write_address($4000 + 256*SPRITEIMG)

    ldy #0
!:
    //txa
    //and #$03
    lda spriteData,y
    jsr spi_write_byte
    iny
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

framerateColors:
    .byte 0, 0, $0f, $1f, $ff

message:
    .text "THIS IS A SHORT TEST OF THE FUNCTIONALITY OF GAMEDUINO."
    .text "I NEED TO WRITE SOMETHING CLEVER HERE TO FILL THE MEMORY"
    .text "WITH DATA. IT IS GETTING LATE AND I THINK I SHOULD REALLY"
    .text "GO TO SLEEP. END OF MESSAGE. I PROMISE. HERP. DERP. FOR NOW."

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

#import "../system/spi.asm"

    .align $100
prgEnd:
}

* = $0001 virtual
.zp {
relPosition:
    .byte 0
position:
    .byte 0
vb:
    .byte 0
text_counter:
    .byte 0
}

* = $0200 virtual
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

