#importonce

.function get15bit(rgb) {
    .var r = (rgb >> (16+3)) & 31
    .var g = (rgb >> (8+3)) & 31
    .var b = (rgb >> (3)) & 31

    .return (r << 10) | (g << 5) | b
}
.function rgbToIndex(rgb) {
    .var r = (rgb >> 21) & 7
    .var g = (rgb >> 11) & 7
    .var b = (rgb >> 6) & 3

    .return (r << 6) | (g << 3) | b
}

.function get_color_list(img, xofs, yofs) {
    .var colors = List()
    .for (var y = 0; y < 16; y++) {
        .for (var x = 0; x < 16; x++) {
            .var color = img.getPixel(xofs+x,yofs+y)
            .var c15 = get15bit(color)
            .eval colors.add(c15)
            .print toBinaryString(color) +" => " + toBinaryString(c15)
        }
    }
    .return colors
}

.macro store_palette_lo(color_list) {
    .for (var i = 0; i < 256; i++) {
        .byte <color_list.get(i)
    }
}

.macro store_palette_hi(color_list) {
    .for (var i = 0; i < 256; i++) {
        .byte >color_list.get(i)
    }
}
