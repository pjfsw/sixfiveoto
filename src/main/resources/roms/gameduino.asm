.cpu _65c02
.encoding "ascii"

.label VIA = $D000
.label PORTB = VIA
.label PORTA = VIA+1
.label DDRB = VIA+2
.label DDRA = VIA+3
.label SPI_PORT = PORTA

.const CLOCK = $01
.const SS = $02
.const MOSI = $40
.const MISO = $80

.const A_OUTPUTS = CLOCK | SS | MOSI
.const CLOCK0_SELECT = (SS | CLOCK) ^ $ff
.const CLOCK0_IDLE = CLOCK ^ $ff
.const WRITE_1 = MOSI
.const WRITE_0 = $00

.label TEXTPTR = $01
.label IDENTIFIER = $0200
.label COLOR = $0201

* = $F000
    lda #A_OUTPUTS
    sta DDRA

    lda #CLOCK0_SELECT
    sta SPI_PORT
    lda #$28
    jsr spi_transfer
    lda #$00
    jsr spi_transfer
    jsr spi_transfer
    sta IDENTIFIER
    lda #CLOCK0_IDLE
    sta SPI_PORT

    lda #CLOCK0_SELECT
    sta SPI_PORT
    lda #$80
    jsr spi_transfer
    lda #$00
    jsr spi_transfer

    lda #<text
    sta TEXTPTR
    lda #>text
    sta TEXTPTR+1
    lda (TEXTPTR)
!:
    {
        jsr spi_transfer
        inc TEXTPTR
        bne !+
        inc TEXTPTR+1
    !:
    }
    lda (TEXTPTR)
    bne !-

    lda #CLOCK0_IDLE
    sta SPI_PORT

loop:
    // Set background color
    lda #CLOCK0_SELECT
    sta SPI_PORT
    lda #($80 | $28)
    jsr spi_transfer
    lda #$0e
    jsr spi_transfer
    lda COLOR
    and #$1f
    jsr spi_transfer
    lda #CLOCK0_IDLE
    sta SPI_PORT
    inc COLOR
!:
    lda $8000
    beq !-
    jmp loop

spi_transfer:
    ldx #WRITE_0
    ldy #WRITE_1
    clc
    .for (var i = 0; i < 8; i++) {
        stx SPI_PORT    // default MOSI = 0
        rol             // shift MSB into carry
        bcc !+          // if carry clear we are done (is 0)
        sty SPI_PORT    // carry set, MOSI = 1
    !:
        inc SPI_PORT    // raise clock
        dec SPI_PORT    // clear clock
        and #$fe        // clear lowest bit
        bit SPI_PORT
        bpl !+          // input=0, skip
        ora #$01        // set lowest bit
    !:
    }
    rts

text:
    .text "GAMEDUINO ASCII TEST FROM 6502!"
    .byte 0