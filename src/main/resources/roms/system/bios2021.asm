    .cpu _65c02'

    #import "load_address.asm"
    #import "pins.asm"

    .const BYTE_READ_SEQUENCE = 3

    *=$ff00 "Implementation"
start:
    // No interrupts during boot loader
    sei

    // Initialize ports
    lda #A_OUTPUTS
    sta DDRA
    lda #B_OUTPUTS
    sta DDRB

    // Enable serial memory
    lda #CART_SELECT
    sta SS_PORT

    // Read bytes from beginning of serial memory.
    lda #BYTE_READ_SEQUENCE
    jsr writeByte
    lda #0
    jsr writeByte
    jsr writeByte
    jsr writeByte

    // Set destination address
    stz loadPtr
    lda #>LOAD_ADDRESS
    sta loadPtr+1

    // Load 126 pages = 32K - 512 bytes
    ldx #0
!:  {
        ldy #0
    !:
        jsr readByte
        sta (loadPtr),y
        iny
        bne !-
    }
    inc loadPtr+1
    inx
    cpx LOAD_PAGES  // First byte of loaded program contains the length to load in pages
    bcc !-

    // Disable serial memory
    lda #$FF
    sta SS_PORT

    jmp START_ADDRESS

readByte:
    stx.z x

    ldx #$7F            // +2
    .for (var i = 0; i < 8; i++) { // 18 cycles
        inc SPI_PORT    // +6
        cpx SPI_PORT    // +4
        rol             // +2
        dec SPI_PORT    // +6
    }
    eor #$ff            // 2  = 148 cycles
    ldx.z x
    rts

writeByte:
    sta.z a

    ldx #MOSI       // +2
    ldy #8
    !: {
        stz SPI_PORT    // +4   default MOSI = 0
        asl             // +2   shift MSB into carry, shift 0 into LSB
        bcc !+          // +2/3 if carry clear we are done (is 0)
        stx SPI_PORT    // +4   carry set, MOSI = 1
    !:
        inc SPI_PORT    // +6   raise clock
        dec SPI_PORT    // +6   clear clock
    }
    dey
    bne !-

    lda.z a
    rts

    * = $fffa "6502 vectors"
    .byte <NMI, >NMI
    .byte <start, >start
    .byte <IRQ, >IRQ

*=$02 virtual
.zp {
    loadPtr: .byte 0,0
    a: .byte 0
    x: .byte 0
    y: .byte 0
 }
