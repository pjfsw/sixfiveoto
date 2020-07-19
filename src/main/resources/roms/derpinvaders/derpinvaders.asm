.cpu _65c02
.encoding "ascii"

#import "../system/load_address.asm"
#import "../system/pins.asm"

.pseudopc LOAD_ADDRESS {
    .byte >(prgEnd-*)

loop_forever:
    jsr title_screen
    jsr game
    jsr game_over
    jmp loop_forever

#import "title_screen.asm"
#import "game.asm"
#import "game_over.asm"

#import "../system/spi.asm"

    .align $100
prgEnd:
}
