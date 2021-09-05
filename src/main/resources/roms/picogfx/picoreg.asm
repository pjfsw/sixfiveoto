#importonce

.const D = $c000
.const PAGE = $c001
.const AH = $c003
.const AL = $c002

.const pico_no_of_sprites = 16

.const pico_scr_0         = $0000
.const pico_col_0         = $1000
.const pico_scr_1         = $2000
.const pico_col_1         = $3000
.const pico_font_0        = $4000
.const pico_font_1        = $5000
.const pico_font_select   = $6480
.const pico_sprite_x      = $6400
.const pico_sprite_y      = $6420
.const pico_sprite_height = $6440
.const pico_sprite_ptr    = $6450
.const pico_scroll_y      = $6460
.const pico_scroll_x      = $6462
.const pico_screen_select = $6464
// Reserved
.const pico_bitmap_start  = $646a // 0-1023, 512 = first visible row
.const pico_bitmap_height = $646c
.const pico_bitmap_ptr    = $646e
.const pico_bitmap_pal    = $6470