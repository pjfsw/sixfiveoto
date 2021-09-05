.cpu _65c02
.encoding "ascii"

#import "picoreg.asm"
#import "loadtarget.asm"

.var jifTemplate = "Id=0,Width=1,Height=2,PaletteSize=4,Data=5"
.var jif = LoadBinary("sixteencolors.jif", jifTemplate)
.var width = jif.ugetWidth(0)
.var height = (jif.ugetHeight(0)+(jif.ugetHeight(1)<<8))

.label BITMAP_POINTER = $64c0
.const ZP_PTR = $80

* = LOAD_TARGET
.pseudopc LOAD_TARGET {
load_image:
    sei
    // Copy the image into VRAM
    lda #0
    sta PAGE
    lda #<BITMAP_POINTER
    sta AL
    lda #>BITMAP_POINTER
    sta AH
    // Use zero page for source address
    lda #<pixels
    sta ZP_PTR
    lda #>pixels
    sta ZP_PTR+1
    // Rows in y and cols in x for simplicity
    ldy #height
!:
    jsr padding
    ldx #width
    {
    !:
        lda (ZP_PTR)
        sta D
        inc ZP_PTR
        {
            bne !+
            inc ZP_PTR+1
        !:
        }
        dex
        bne !-
    }
    jsr padding

    dey
    bne !-

    stz PAGE
    lda #<pico_bitmap_start
    sta AL
    lda #>pico_bitmap_start
    sta AH
    ldx #0
!:
    lda bitmap_start,x
    sta D
    inx
    cpx #22
    bne !-
    rts

padding:
    // Pad rest of line with zero in a stupid and inefficient way
    ldx #(200-width)/2
    {
    !:
        stz D
        dex
        bne !-
    }
    rts

pixels:
    .print "Width * Height = " + width + " * " + height
    .fill width * height, jif.getData(i+jif.getPaletteSize(0))
bitmap_start:
    .word 528           // first visible row
    .word height        // bitmap height
    .byte <(BITMAP_POINTER/2), >(BITMAP_POINTER/2)  // bitmap pointer in video RAM
palette:
    .fill 16,jif.getData(i)

}

