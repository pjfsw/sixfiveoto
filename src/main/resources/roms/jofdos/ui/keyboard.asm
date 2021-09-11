#importonce

.const KEY_SHIFT = $80
.const KEY_ALT = $81
.const KEY_UP = $E0
.const KEY_DOWN = $E1
.const KEY_LEFT = $E2
.const KEY_RIGHT = $E3
.const KEY_ENTER = $0d
.const KEY_BACKSPACE = $08

.const CLOCK = $01
.const KEYBOARD_SELECT = $FE
.const NO_SELECT = $FF

.segment DosCode

readSpiByte:
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

readKeyboard:
    ldx #KEYBOARD_SELECT
    stx SS_PORT
    jsr readSpiByte
    ldx #NO_SELECT
    stx SS_PORT
    tax
    lda keyCodes,x
    rts

keyCodes:
    .byte 0
    .fill 26, i+'a'  // $01
    .byte KEY_ENTER, KEY_BACKSPACE, 0,0,0        // $1b
    .byte ' '        // $20
    .byte 0,0,0,0,0,0,$27,0,0,0 // $21
    .byte '+'        // $2b
    .byte ','        // $2c
    .byte '-'        // $2d
    .byte '.'        // $2e
    .byte 0          // $2f

    .fill 10, '0'+i  // $30
    .byte 0,0,'<',0,0,0 // $3a

    .print *-keyCodes
    // ALT not supported atm
    .fill 64,0

    // SHIFT
    .byte 0          // $00
    .fill 26,i+'A'   // $01
    .fill 5,0        // $1b
    .byte ' '        // $20
    .byte 0,0,0,0,0,0,'*',0,0,0 // $21
    .byte '?'        // $2b
    .byte ';'        // $2c
    .byte '_'        // $2d
    .byte ':'        // $2e
    .byte 0          // $2f

    .text @"=!\"#$%&/()" // $30
    .byte 0,0,'>',0,0,0 // $3a

    .fill 64,0
