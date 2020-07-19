#importonce

.function gd_pos(x,y) {
    .return y * 64 + x
}

.function gd_centre(len) {
    .return (50-len)/2
}

.macro gd_clear(gd_address, length) {
    gd_write_address(gd_address)
    {
        .for (var i = 0; i < length; i++) {
            jsr spi_write_zero
        }
    }
    spi_end()
}

.macro gd_copy_bytes(gd_address, memory_address, length) {
    gd_write_address(gd_address)
    {
        ldy #0
    !:
        lda memory_address,y
        jsr spi_write_byte
        iny
        cpy #length
        bne !-
    }
    spi_end()
}

.macro gd_copy_palette(palette, palette_lo, palette_hi) {
    gd_write_address($3800 + palette*512)
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
}

gd_clear_screen:
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