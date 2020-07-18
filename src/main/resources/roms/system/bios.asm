.cpu _65c02
.encoding "ascii"

#import "pins.asm"
#import "load_address.asm"

* = $0001 virtual
 .zp {
loadPosition:
    .byte 0
loadPointer:
    .byte 0,0
firstFrame:
    .byte 0
wait_count:
    .byte 0
}

* = $F000
    spi_end()
    lda #A_OUTPUTS
    sta DDRA
    lda #B_OUTPUTS
    sta DDRB

    // Reset scroll
    gd_write_address($2804)
    {
        jsr spi_write_zero
        jsr spi_write_zero
        jsr spi_write_zero
        jsr spi_write_zero
    }
    spi_end()

//    jsr wait
    jsr drop_sprites
    jsr draw_logo
    jsr clear_screen
    jsr welcome
    lda #144
    sta wait_count
    jsr wait
    lda #1
    sta wait_count
    jsr fade_out

    jsr load_program
    jmp START_ADDRESS

clear_screen:
    gd_write_address(0)
    {
        ldy #0
    !:
        .for (var i = 0; i < 16; i++) {
            jsr spi_write_zero
        }
        dey
        bne !-
    }
    spi_end()
    rts

draw_logo:
    gd_write_address($4000)
    {
        ldy #0
    !:
        tya
        jsr spi_write_byte
        iny
        bne !-
    }
    spi_end()
    gd_write_address($3800)
    {
        .for (var i = 0; i < 4; i++) {
            ldy #0
        !:
            lda palette_lo+i*256,y
            jsr spi_write_byte
            lda palette_hi+i*256,y
            jsr spi_write_byte
            iny
            bne !-
        }
    }
    spi_end()
    gd_write_address($3000)
    {
        .for (var i = 0; i < 4; i++) {
            spi_write(184+mod(i,2)*16)
            lda #i * $10 // Palette select, $00, $10, $20, $30
            jsr spi_write_byte
            spi_write(112+floor(i/2)*16)
            jsr spi_write_zero
        }
    }
    spi_end()

    rts

welcome:
    // Set background color to black
    gd_write_address($280e)
    {
        jsr spi_write_zero
        jsr spi_write_zero
    }
    spi_end()

    // Show fancy message
    ldy #0
    gd_write_address(64*19+(50-welcome_msg_length)/2)
    {
    !:
        lda welcome_msg,y
        jsr spi_write_byte
        iny
        cpy #welcome_msg_length
        bne !-
    }
    spi_end()
    rts

wait:
    ldy #0

!:
    gd_read_address($2802)
    {
        jsr spi_read_byte
        tax
    }
    spi_end()
    txa
    {
        cpy #0
        bne !+
        sta firstFrame
    !:
        ldy #1
    }
    sec
    txa
    sbc firstFrame
    cmp wait_count
    bcc !-

    rts

drop_sprites:
    ldy #0
    gd_write_address($3000)
    {
    !:
        spi_write(255)
        spi_write(1)
        spi_write(255)
        spi_write(1)
        dey
        bne !-
    }
    spi_end()
    rts

load_program:
    lda #<LOAD_ADDRESS
    sta loadPointer
    lda #>LOAD_ADDRESS
    sta loadPointer+1

    stz loadPosition

    cart_read()
    {
        jsr spi_write_zero
        jsr spi_write_zero
        jsr spi_write_zero

    !:
        {
            ldy #0
        !:
            jsr spi_read_byte
            sta (loadPointer),y
            iny
            bne !-
        }

        inc loadPointer+1
        inc loadPosition
        lda loadPosition
        cmp LOAD_ADDRESS
        bcc !-
    }
    spi_end()

    rts

fade_out:
    ldy #$fc
!:
    // Reset scroll
    gd_write_address($2806)
    {
        tya
        jsr spi_write_byte
        lda #1
        jsr spi_write_byte
    }
    spi_end()
    phy
    jsr wait
    ply
    dey
    dey
    dey
    dey

    bne !-

    jsr clear_screen
    jsr drop_sprites
    rts
welcome_msg:
    .text "JOFMODORE ENTERTAINMENT SYSTEM V0.9"
.label welcome_msg_length = * - welcome_msg

.function get15bit(rgb) {
    .var r = (rgb >> (16+3)) & 31
    .var g = (rgb >> (8+3)) & 31
    .var b = (rgb >> (3)) & 31

    .return (r << 10) | (g << 5) | b
}
.function rgbToIndex(rgb) {
    .var r = (rgb >> 21) & 7
    .var g = (rgb >> 11) & 7
    .var b = (rgb >> 6) & 3

    .return (r << 6) | (g << 3) | b
}

.function get_color_list(img, xofs, yofs) {
    .var colors = List()
    .for (var y = 0; y < 16; y++) {
        .for (var x = 0; x < 16; x++) {
            .var color = img.getPixel(xofs+x,yofs+y)
            .var c15 = get15bit(color)
            .eval colors.add(c15)
            .print toBinaryString(color) +" => " + toBinaryString(c15)
        }
    }
    .return colors
}

.macro store_palette_lo(color_list) {
    .for (var i = 0; i < 256; i++) {
        .byte <color_list.get(i)
    }
}

.macro store_palette_hi(color_list) {
    .for (var i = 0; i < 256; i++) {
        .byte >color_list.get(i)
    }
}

.var logoFile = LoadPicture("girl.jpg")
.var xofs = 24
.var yofs = 24

palette_lo:
    .for (var y = 0; y < 2; y++) {
        .for (var x = 0; x < 2; x++) {
            store_palette_lo(get_color_list(logoFile,xofs+x*16,yofs+y*16))
        }
    }

palette_hi:
    .for (var y = 0; y < 2; y++) {
        .for (var x = 0; x < 2; x++) {
            store_palette_hi(get_color_list(logoFile,xofs+x*16,yofs+y*16))
        }
    }



#import "spi.asm"


