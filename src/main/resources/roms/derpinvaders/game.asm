#importonce

#import "gd_common.asm"
#import "../system/sprites.asm"
#import "constants.asm"

.const SPEED = 2
.const MAX_X = 192
.const MAX_Y = 142
.const STAR = 128
.const STAR_FIX = 131

game:
    gd_clear($2804, 4)
    jsr load_sprites
    jsr init_positions
    jsr init_stars

gameloop:
    jsr draw_stars
    jsr draw_sprites
    jsr next_frame

//
//.const PIN_JOY_UP = 3
//.const PIN_JOY_DOWN = 4
//.const PIN_JOY_LEFT = 5
//.const PIN_JOY_RIGHT = 6
//.const PIN_JOY_A = 7
    lda JOY_PORT
    asl
    bcc done

// Check right
    asl
    bcs !+

    tax
    clc
    lda x
    adc #SPEED
    {
        cmp #MAX_X
        bcc !+
        lda #MAX_X
    !:
    }
    sta x
    txa

// Check left
!:
    asl
    bcs !+

    tax
    sec
    lda x
    sbc #SPEED
    {
        cmp #MAX_X+1
        bcc !+
        lda #0
    !:
    }
    sta x
    txa

// Check down
!:
    asl
    bcs !+

    tax
    clc
    lda y
    adc #SPEED
    {
        cmp #MAX_Y
        bcc !+
        lda #MAX_Y
    !:
    }
    sta y
    txa

// Check up
!:
    asl
    bcs gameloop

    sec
    lda y
    sbc #SPEED
    {
        cmp #MAX_X+1
        bcc !+
        lda #0
    !:
    }
    sta y
    jmp gameloop

done:
    rts

next_frame:
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
        sta frame_counter
    !:
        ldy #1
    }
    cmp frame_counter
    beq !-
    rts

draw_sprites:
    gd_write_address($3000)
    {
        .for (var i = 0; i < SPRITE_COUNT; i++) {
            lda x + i
            tay
            asl
            jsr spi_write_byte
            cpy #$80
            rol
            jsr spi_write_byte
            lda y + i
            tay
            asl
            jsr spi_write_byte
            cpy #$80
            rol
            ora src_img + i
            jsr spi_write_byte
            // ORA source image
        }
    }
    spi_end()
    rts

load_sprites:
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
    gd_copy_palette(0, player_palette_lo, player_palette_hi)
    rts

draw_stars:
    inc stars
    inc stars
    inc stars
    inc stars
    lda stars
    and #$f0
    tay
    gd_write_address($1000 + STAR*16)
    {
        // Update two stars
        .for (var i = 0; i < 16; i++) {
            lda star_buffer,y
            jsr spi_write_byte
            jsr spi_write_zero
            iny
        }
    }
    spi_end()
    dec scroll
    bne !+
    dec scroll+1
!:
    gd_write_address($2806)
    {
        lda scroll
        jsr spi_write_byte
        lda scroll+1
        and #1
        jsr spi_write_byte
    }
    spi_end()

    rts

init_stars:
    .for (var i = 0; i < 16; i++) {
        gd_copy_bytes(i*256, starfield, 256)
    }
    gd_copy_bytes($1000+STAR_FIX*16, starfont, 16)
    gd_copy_bytes($2000+STAR*8, star_palette_bright, 8)
    gd_copy_bytes($2000+(STAR+1)*8, star_palette_bright, 8)
    gd_copy_bytes($2000+(STAR_FIX)*8, star_palette_dark, 8)
    rts

init_positions:
    lda #96
    sta x

    lda #127
    sta y
    rts

// We shift coordinates by 1 to eliminate the high byte, we want higher movement speed anyway
x:
    .fill SPRITE_COUNT, 0
y:
    .fill SPRITE_COUNT, 0
src_img:
    .fill SPRITE_COUNT, 0
frame_counter:
    .byte 0
scroll:
    .word 0
stars:
    .byte 0

    .align $100
star_buffer:
    .for (var i = 0; i < 16; i++) {
        .for (var j = 0; j < 16; j++) {
            .if (i == j) {
                .byte $03
            } else {
                .byte $00
            }
        }
    }

starfield:
    .for (var y = 0; y < 64; y++) {
        .for (var x = 0; x < 64; x++) {
            .var yoffset = mod(x, 3)
            .if (mod(x,13)==0) {
                .if (mod(y+yoffset,2) == 0) {
                    .byte STAR
                } else {
                    .byte STAR+1
                }
            } else .if (random() < 0.1) {
                .byte STAR_FIX
            } else {
                .byte 0
            }
        }
    }

starfont:
    .word $0300
    .word $0000
    .word $0000
    .word $0000
    .word $0000
    .word $0000
    .word $0000
    .word $0000

star_palette_bright:
    .word $8000
    .word 0
    .word 0
    .word %0011110111101111 // grayish

star_palette_dark:
    .word $8000
    .word 0
    .word 0
    .word %0010000100001000 // grayish

.var player_image = LoadPicture("spaceship.png")
player_palette_lo:
    store_palette_lo(get_color_list(player_image,0,0))

player_palette_hi:
    store_palette_hi(get_color_list(player_image,0,0))


