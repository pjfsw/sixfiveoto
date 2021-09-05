#importonce

#import "loadtarget.asm"

.const LOAD_PTR_ZP = $f0
.const MOSI = 64;

spi_read_byte:
    ldy #1              // +2
    ldx #$7F            // +2
    .for (var i = 0; i < 8; i++) { // 14 cycles
        sty SPI_PORT    // +4
        cpx SPI_PORT    // +4
        rol             // +2
        stz SPI_PORT    // +4
    }
    eor #$ff            // +2
    rts

spi_write_byte:
    ldx #MOSI           // +2
    .for (var i = 0; i < 8; i++) { // $00: 21  $FF: 24 cycles
        stz SPI_PORT    // +4   default MOSI = 0
        asl             // +2   shift MSB into carry, shift 0 into LSB
        bcc !+          // +2/3 if carry clear we are done (is 0)
        stx SPI_PORT    // +4   carry set, MOSI = 1
    !:
        inc SPI_PORT    // +6   raise clock
        dec SPI_PORT    // +4   clear clock
    }                   // Min: 27*8+4=172 cycles  Max: 31*8+4=196 cycles
    rts

load_rom:
    lda #$ff-2          // Select ROM
    sta SS_PORT

    lda #$03            // Read cmd
    jsr spi_write_byte

    lda #0              // Read from beginning of cartridge
    jsr spi_write_byte
    lda #0
    jsr spi_write_byte
    lda #0
    jsr spi_write_byte

    lda #<LOAD_TARGET
    sta LOAD_PTR_ZP
    lda #>LOAD_TARGET
    sta LOAD_PTR_ZP+1

!:
    jsr spi_read_byte
    sta (LOAD_PTR_ZP)
    inc LOAD_PTR_ZP
    bne !-
    lda LOAD_PTR_ZP+1
    clc
    adc #1
    sta LOAD_PTR_ZP+1
    cmp #$80            // load 32k
    bne !-

    lda #$ff           // Unselect ROM
    sta SS_PORT

    rts