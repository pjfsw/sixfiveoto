#importonce

#import "gd_common.asm"
#import "../system/sprites.asm"
#import "constants.asm"

.const SPEED = 2
.const MAX_X = 192
.const MAX_Y = 142

game:
    jsr gd_clear_screen
    gd_clear($2804, 4)
    jsr load_sprites
    jsr init_positions

gameloop:
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

init_positions:
    lda #192
    stz x

    lda #255
    stz y
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

.var player_image = LoadPicture("spaceship.png")
player_palette_lo:
    store_palette_lo(get_color_list(player_image,0,0))

player_palette_hi:
    store_palette_hi(get_color_list(player_image,0,0))


