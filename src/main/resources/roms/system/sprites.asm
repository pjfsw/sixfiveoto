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

.function get_color_list(img) {
    .var colorMap = Hashtable()
    .var max = 0
    .for (var y = 0; y < img.height; y++) {
        .for (var x = 0; x < img.width; x++) {
            .var color = img.getPixel(x,y)
            .var c15 = get15bit(color)
            .if (colorMap.containsKey(c15)) {
                .var count = colorMap.get(c15)+1
                .eval colorMap.put(c15, count)
                .if (count > max) {
                    .eval max = count
                }
            } else {
                .eval colorMap.put(c15,1)
            }
            //.eval colors.add(c15)
            //.print toBinaryString(color) +" => " + toBinaryString(c15)
        }
    }
    .var colorList = List()
    .for (var c = max; (c > 0) && (colorList.size() < 256) ; c--) {
        //.print "Scanning max colors " + c
        .var keys = colorMap.keys()
        .for (var i = 0; i < keys.size(); i++) {
            .var key = keys.get(i)
            .if (colorMap.get(key) == c) {
                .eval colorList.add(key.asNumber())
            }
        }
    }

    .var newList = List()
    .for (var i = 0; i < 256; i++) {
        .if (i >= colorList.size()) {
            .eval newList.add(0)
        } else {
            .eval newList.add(colorList.get(i))
        }
    }

    .return newList
}

.function get_palette_index(color, color_list) {
    .var minDistance = $ffffffff
    .var minIndex = 0

    .var r = (color >> 10) & 31
    .var g = (color >> 5) & 31
    .var b = color & 31

    .print ("Get palette for " + color)
    .for (var i = 0; i < 256; i++) {
        .var colori = color_list.get(i)
        .var ri = (colori >> 10) & 31
        .var gi = (colori >> 5) & 31
        .var bi = colori & 31
        .var rd = r-ri
        .var gd = g-gi
        .var bd = b-bi
        .var distance = rd*rd + gd*gd + bd*bd
        .if (distance < minDistance) {
            .eval minDistance = distance
            .eval minIndex = i
        }
    }
    .print "Matching color " + color + " to index " + minIndex + " = " + color_list.get(minIndex)
    .return minIndex
}

.macro store_palette_lo(color_list) {
    .for (var i = 0; i < 256; i++) {
        .byte <color_list.get(i)
    }
}

.macro store_palette_hi(color_list) {
    .for (var i = 0; i < 256; i++) {
        .var c = color_list.get(i)
        .if (c == 0) {
            .byte $80
        } else {
            .byte >color_list.get(i)
        }
    }
}
