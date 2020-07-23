.cpu _65c02
.encoding "ascii"

* = $F000
start:
    jmp *

* = $FFFC
    .word start
