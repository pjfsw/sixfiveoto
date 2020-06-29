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

public enum LdIndexed implements Instruction {
    ANDX(Registers::a, Registers::x, Operation.AND, 0x3D, "AND $%04X,X"),
    ANDY(Registers::a, Registers::y, Operation.AND, 0x39, "AND $%04X,X"),
    LDAX(Registers::a, Registers::x, Operation.EQU, 0xBD, "LDA $%04X,X"),
    LDAY(Registers::a, Registers::y, Operation.EQU, 0xB9, "LDA $%04X,Y"),
    LDXY(Registers::x, Registers::y, Operation.EQU, 0xBE, "LDX $%04X,Y"),
    LDYX(Registers::y, Registers::x, Operation.EQU, 0xBC, "LDY $%04X,X"),
    EORX(Registers::a, Registers::x, Operation.EOR, 0x5D, "EOR $%04X,X"),
    EORY(Registers::a, Registers::y, Operation.EOR, 0x59, "EOR $%04X,X"),
    ORAX(Registers::a, Registers::x, Operation.ORA, 0x1D, "ORA $%04X,X"),
    ORAY(Registers::a, Registers::y, Operation.ORA, 0x19, "ORA $%04X,X");


    private final int opcode;
    private final String mnemonic;
    private final BiConsumer<Registers, Integer> to;
    private final Function<Registers, Integer> index;
    private final Operation operation;

    LdIndexed(BiConsumer<Registers,Integer> to, Function<Registers,Integer> index, Operation operation, int opcode, String mnemonic) {
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
        int baseAddress = Memory.readWord(peeker, registers.pc);
        int offsetAddress = Memory.add(baseAddress, index.apply(registers));

        to.accept(registers, operation.apply(registers,peeker.peek(offsetAddress)));
        int cycles = 4 + Memory.penalty(baseAddress, offsetAddress);
        registers.incrementPc(2);
        return cycles;
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
