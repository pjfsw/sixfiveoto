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
waitCount:
    .byte 0
}

* = $F000
    lda #A_OUTPUTS
    sta DDRA
    lda #B_OUTPUTS
    sta DDRB

    jsr wait
    jsr clear_screen
    jsr welcome
    jsr drop_sprites
    jsr wait

    jsr load_program
    jmp START_ADDRESS

clear_screen:
    gd_write_address(0)
    {
        ldy #0
    !:
        .for (var i = 0; i < 16; i++) {
            lda #0
            jsr spi_write_byte
        }
        dey
        bne !-
    }
    spi_end()
    rts

welcome:
    // Set background color to black
    gd_write_address($280e)
    {
        spi_write(0)
        spi_write(0)
    }
    spi_end()

    // Show fancy message
    ldy #0
    gd_write_address(64*10)
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
    cmp #144
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
        lda #0
        jsr spi_write_byte

        lda #0
        jsr spi_write_byte

        lda #0
        jsr spi_write_byte

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

welcome_msg:
    .text " JOFMODORE SYSTEM V0.9 - (C)2020 JOHAN FRANSSON"
.label welcome_msg_length = * - welcome_msg


#import "spi.asm"


