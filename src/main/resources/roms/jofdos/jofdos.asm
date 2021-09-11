.cpu _65c02
.encoding "ascii"

.segmentdef DosCode [start=$e000]
.segmentdef DosVars [start=$0200, virtual, min=$0200, max=$02ff]
.segmentdef Zeropage [start=$0000, virtual, min=$00, max=$ff]

.const IRQ = $0300

.const SPI_VIA = $c800
.const SS_PORT = SPI_VIA
.const SPI_PORT = SPI_VIA+1
.const SS_DDR = SPI_VIA+2
.const SPI_DDR = SPI_VIA+3

.const BGCOLOR = %010101
.const TEXTCOLOR = %111111

#import "ui/readline.asm"
#import "ui/print.asm"
#import "ui/startup.asm"
#import "filesystem/dir.asm"
#import "commands/command.asm"
#import "filesystem/filesystem.asm"

.segment DosCode

start:
    jsr setupPorts
    jsr copyIrq

    jsr startupScreen

    stz cursorX
    lda #2
    sta cursorY

!:
    lda #'$'
    jsr printChar
    lda #' '
    jsr printChar
    jsr readline
    jsr parseCommand
    jmp !-

copyIrq: // IRQ vector is hardwired to point at RAM address so we need to copy ours to RAM
    ldx #0
!:
    lda irqSource,x
    sta IRQ,x
    inx
    cpx #irqLength
    bcc !-
    cli
    rts


setupPorts:
    stz SPI_PORT
    lda #%01000001
    sta SPI_DDR
    lda #$FF
    sta SS_PORT
    lda #%00001111
    sta SS_DDR
    rts

valueErrMsg:
    .text "Argument must be 1-4 byte hex!"
.label valueErrLength = *-valueErrMsg

rowToPixelLo:
    .fill 64,<(i*8)
rowToPixelHi:
    .fill 64,>(i*8)

.macro scrollX(address) {
    lda #$62
    sta AL
    lda #$64
    sta AH
    lda address
    sta D
    lda address+1
    sta D
}

irqSource:
.pseudopc IRQ {
irq:
    stx irqX
    sty irqY
    sta irqA
    sta SAVEADDR

    inc cursorBlink

    sta RESTADDR
    ldx irqX
    ldy irqY
    lda irqA
    rti
}
.label irqLength = *-irqSource

* = $FFFA "Vectors"
    .word 0
    .word start
    .word IRQ

.segment Zeropage
.zp {
ioAddress:
    .word 0
ioCount:
    .byte 0
}

.segment DosVars

irqX:
    .byte 0
irqY:
    .byte 0
irqA:
    .byte 0
cursorX:
    .byte 0
cursorY:
    .byte 0
cursorBlink:
    .byte 0
readBuffer:
    .fill 64,0
readBufferSize:
    .byte 0
scrollOffset:
    .byte 0
inputOffset:
    .byte 0
jumpPointer:
    .word 0
