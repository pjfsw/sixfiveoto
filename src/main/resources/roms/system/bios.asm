.cpu _65c02
.encoding "ascii"

#import "pins.asm"
#import "load_address.asm"
#import "sprites.asm"

.const LOGO_W = 3
.const LOGO_H = 2

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
        .for (var sprite = 0; sprite < LOGO_W * LOGO_H; sprite++) {
            ldy #0
        !:
            lda image_data+sprite*256,y
            jsr spi_write_byte
            iny
            bne !-
        }
    }
    spi_end()
    gd_write_address($3800)
    {
        ldy #0
    !:
        lda palette_lo,y
        jsr spi_write_byte
        lda palette_hi,y
        jsr spi_write_byte
        iny
        bne !-
    }
    spi_end()
    gd_write_address($3000)
    {
        .for (var y = 0; y < LOGO_H; y++) {
            .for (var x = 0; x < LOGO_W; x++) {
                spi_write(152+x*16)
                //lda #i * $10 // Palette select, $00, $10, $20, $30
                //jsr spi_write_byte
                jsr spi_write_zero
                spi_write(112+y*16)
                spi_write((y*LOGO_W+x) << 1)
            }
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


.var logoFile = LoadPicture("dystopia.jpg")

.var logo_colors = get_color_list(logoFile)

image_data: // 6 x 8
    .for (var y = 0; y < LOGO_H; y++) {
        .for (var x = 0; x < LOGO_W; x++) {
            .for (var py = 0; py < 16; py++) {
                .for (var px = 0; px < 16; px++) {
                    .byte get_palette_index(get15bit(logoFile.getPixel(x*16+px, y*16+py)), logo_colors)
                }
            }
        }
    }

palette_lo:
    store_palette_lo(logo_colors)

palette_hi:
    store_palette_hi(logo_colors)



#import "spi.asm"


