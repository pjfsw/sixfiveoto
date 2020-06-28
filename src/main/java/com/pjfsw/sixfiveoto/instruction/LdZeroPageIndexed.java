package com.pjfsw.sixfiveoto.instruction;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum LdZeroPageIndexed implements Instruction {
    ANDX(Registers::a, Registers::x, Operation.AND, 0x35, "AND $%02X,X"),
    EORX(Registers::a, Registers::x, Operation.EOR, 0x55, "EOR $%02X,X"),
    LDAX(Registers::a, Registers::x, Operation.EQU, 0xB5, "LDA $%02X,X"),
    LDXY(Registers::x, Registers::y, Operation.EQU, 0xB6, "LDX $%02X,Y"),
    LDYX(Registers::y, Registers::x, Operation.EQU, 0xB4, "LDY $%02X,X"),
    ORAX(Registers::a, Registers::x, Operation.ORA, 0x15, "ORA $%02X,X");


    private final int opcode;
    private final String mnemonic;
    private final BiConsumer<Registers, Integer> to;
    private final Function<Registers, Integer> index;
    private final Operation operation;

    LdZeroPageIndexed(BiConsumer<Registers,Integer> to, Function<Registers,Integer> index, Operation operation, int opcode, String mnemonic) {
        this.to = to;
        this.index = index;
        this.operation = operation;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        to.accept(registers, operation.apply(registers,peeker.peek(
            Memory.add(peeker.peek(registers.pc), index.apply(registers)))));
        registers.incrementPc(1);
        return 4;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format(mnemonic, parameter);
    }

    @Override
    public List<Integer> assemble(final Integer parameter) {
        return ImmutableList.of(opcode(), Word.lo(parameter), Word.hi(parameter));
    }
}
