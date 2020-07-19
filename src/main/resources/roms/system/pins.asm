#importonce

.label VIA = $D000
.label PORTB = VIA
.label PORTA = VIA+1
.label DDRB = VIA+2
.label DDRA = VIA+3
.label SR = VIA+10

.label SS_PORT = PORTB
.const GD_SS = 1 << 1
.const CART_SS = 1 << 2

.label SPI_PORT = PORTA
.const CLOCK = $01
.const MOSI = 1 << 6
.const MISO = 1 << 7

.const A_OUTPUTS = CLOCK | MOSI
.const B_OUTPUTS = GD_SS | CART_SS

.const GD_SELECT = GD_SS ^ $ff
.const CART_SELECT = CART_SS ^ $ff
.const IDLE = $ff

.const JOY_PORT = PORTB
.const PIN_JOY_UP = 3
.const PIN_JOY_DOWN = 4
.const PIN_JOY_LEFT = 5
.const PIN_JOY_RIGHT = 6
.const PIN_JOY_A = 7
