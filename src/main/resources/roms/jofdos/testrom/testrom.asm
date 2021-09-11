.cpu _65c02
.encoding "ascii"

#import "../picoreg.asm"
#import "../loadtarget.asm"

.var jifTemplate = "Id=0,Width=1,Height=2,PaletteSize=4,Data=5"
.var bmp_jif = LoadBinary("sixteencolors.jif", jifTemplate)
.var bmp_width = bmp_jif.ugetWidth(0)
.var bmp_height = (bmp_jif.ugetHeight(0)+(bmp_jif.ugetHeight(1)<<8))
.var spr_jif = LoadBinary("spritedata.jif", jifTemplate)
.var spr_width = spr_jif.ugetWidth(0)
.var spr_height = (spr_jif.ugetHeight(0)+(spr_jif.ugetHeight(1)<<8))

.label BITMAP_POINTER = $64c0
.label SPRITE_POINTER = $11000
.const ZP_PTR = $80

/*
  Filesystem v0.1

  Works with even pages (256 bytes)

  Page 0 is directory

  12 byte file name
  2 byte storage location in number of pages
  1 byte size in pages
  1 byte page load address
*/
* = $0000
.text "ImageTest"
.fill 12-*,0
.word 1
.byte >image_test_length
.byte 5

* = $0100
.pseudopc LOAD_TARGET {
image_test_start:
load_image:
    jsr copy_bitmap
    jsr copy_sprite
    jsr copy_regs
    rts

copy_bitmap:
    copy_image_data(pixels, BITMAP_POINTER, bmp_width, bmp_height, (200-bmp_width)/2)
    rts

copy_sprite:
    copy_image_data(sprite, SPRITE_POINTER, 16, spr_height, 0)
    rts

.macro copy_image_data(src_addr,vram_addr,w,h,pad_pixels) {
    // Copy the image into VRAM
    lda #(vram_addr > $ffff ? 1 : 0)
    sta PAGE
    lda #<vram_addr
    sta AL
    lda #>vram_addr
    sta AH
    // Use zero page for source address
    lda #<src_addr
    sta ZP_PTR
    lda #>src_addr
    sta ZP_PTR+1
    // Rows in y and cols in x for simplicity
    ldy #h
!:
    .if (pad_pixels > 0) {
        ldx #pad_pixels
        jsr padding
    }
    ldx #w
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
    .if (pad_pixels > 0) {
        ldx #pad_pixels
        jsr padding
    }

    dey
    bne !-

}

copy_regs:
    stz PAGE
    lda #<pico_sprite_x
    sta AL
    lda #>pico_sprite_x
    sta AH
    ldx #0
!:
    lda reg_start,x
    sta D
    inx
    cpx #reg_length
    bne !-
    rts

padding:
    // Pad rest of line with zero in a stupid and inefficient way
    //ldx #(200-bmp_width)/2
    {
    !:
        stz D
        dex
        bne !-
    }
    rts

sprite:
    .print "Sprite width * height = " + spr_width + " * " + spr_height
    .fill spr_width * spr_height, spr_jif.getData(i+spr_jif.getPaletteSize(0))

pixels:
    .print "Width * Height = " + bmp_width + " * " + bmp_height
    .fill bmp_width * bmp_height, bmp_jif.getData(i+bmp_jif.getPaletteSize(0))
reg_start:
spr_x:
    .fillword pico_no_of_sprites, 12 + i * 24
    .fillword pico_no_of_sprites, 33
    .fill pico_no_of_sprites, 24
    .fill pico_no_of_sprites, SPRITE_POINTER>>8
    .word 0 // scroll-y
    .word 0 // scroll-x
    .byte 0 // screen-select
    .fill 5,0 // reserved
bitmap_start:
    .word 528           // first visible row
    .word bmp_height   // bitmap height
    .byte <(BITMAP_POINTER/2), >(BITMAP_POINTER/2)  // bitmap pointer in video RAM
palette:
    .fill 16,bmp_jif.getData(i)
.label reg_length=*-reg_start
    .align $100
.label image_test_length = *-image_test_start
}

