#importonce
.cpu _65c02
.encoding "ascii"

#import "constants.asm"

title_screen:
    gd_write_address($3000)
    {
        ldy #0
    !:
        spi_write($ff)
        spi_write($01)
        jsr spi_write_zero
        jsr spi_write_zero
        iny
        cpy #SPRITE_COUNT
        bne !-
    }
    spi_end()

    jsr gd_clear_screen
    gd_clear($2804, 4)
    gd_copy_bytes(gd_pos(gd_centre(title_msg_len), 16), title_msg, title_msg_len)
    gd_copy_bytes(gd_pos(gd_centre(press_button_len), 18), press_button_msg, press_button_len)

!:
    lda JOY_PORT
    bmi !-

    rts

title_msg:
    .text "DERP INVADERS"
.label title_msg_len=*-title_msg
press_button_msg:
    .text "PRESS A BUTTON TO PLAY"
.label press_button_len=*-press_button_msg

#import "../system/pins.asm"
#import "gd_common.asm"

