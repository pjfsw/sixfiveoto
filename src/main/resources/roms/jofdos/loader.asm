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

compareString:
    tax
!nextChar:
    cpx #0
    beq !endOfSource+
    lda (stringTarget)
    cmp (stringSource)
    bne !mismatch+
    inc stringTarget
    bne !+
    inc stringTarget+1
!:
    inc stringSource
    bne !+
    inc stringSource+1
!:
    dex
    jmp !nextChar-

!endOfSource:
    clc
    lda (stringTarget)
    bne !+
    sec
!:
    rts
!mismatch:
    clc
    rts

load:
    jsr load_dir
    lda #<LOAD_TARGET
    sta loadTarget
    lda #>LOAD_TARGET
    sta loadTarget+1
!:
    lda loadTarget
    clc
    adc #1
    sta stringTarget
    lda loadTarget+1
    sta stringTarget+1
    lda #<argument1
    sta stringSource
    lda #>argument1
    sta stringSource+1
    lda argumentLength
    jsr compareString
    bcs !+
    lda loadTarget
    clc
    adc #$10
    sta loadTarget
    bne !-
    ldx #<notFoundMessage
    ldy #>notFoundMessage
    lda #notFoundLength
    jsr printLine
    rts
!:
    lda #>LOAD_TARGET
    sta loadTarget+1
    lda #2
    sta loadSource
    stz loadSource+1
    lda #124
    sta loadCount
    jsr load_data
    rts

load_data:
    stz loadTarget      // Clear lowbyte, we only load to even pages
    lda #$ff-2          // Select ROM
    sta SS_PORT

    lda #$03            // Read cmd
    jsr spi_write_byte

    lda loadSource+1    // Page high byte = address bit 24-16
    jsr spi_write_byte

    lda loadSource      // Page low byte = address bit 15-8
    jsr spi_write_byte
    lda #0              // Only care about whole 256 byte pages
    jsr spi_write_byte

!:
    jsr spi_read_byte
    sta (loadTarget)
    inc loadTarget
    bne !-
    lda loadTarget+1
    inc loadTarget+1
    dec loadCount      // Decrease number of pages left until we are done
    bne !-

    lda #$ff           // Unselect ROM
    sta SS_PORT

    rts

notFoundMessage:
    .text "Not found!"
.label notFoundLength = *-notFoundMessage