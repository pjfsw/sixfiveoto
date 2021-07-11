.cpu _65c02
.encoding "ascii"

#import "../system/load_address.asm"
#import "../system/pins.asm"

.pseudopc LOAD_ADDRESS {
    .byte >(prgEnd-*)
    .fill START_ADDRESS-*,$EA

    jsr clear_stuff
loop_forever:
    jsr title_screen
    jsr game
    jsr game_over
    jmp loop_forever

clear_stuff:
    // Set background color to black
    gd_write_address($280e)
    {
        jsr spi_write_zero
        jsr spi_write_zero
    }
    spi_end()

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

#import "title_screen.asm"
#import "game.asm"
#import "game_over.asm"

#import "../system/spi.asm"

    .align $100
prgEnd:
}
