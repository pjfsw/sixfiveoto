.cpu _65c02
.encoding "ascii"

#import "../loadtarget.asm"

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
.byte (>image_test_length)+1
.byte 5

* = $0100
image_test_start:
#import "imagetest/imagetest.asm"
.align $100
.label image_test_length = *-image_test_start


