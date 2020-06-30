package com.pjfsw.sixfiveoto.instruction;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum Lda implements Instruction {
    // Absolute
    AND(Registers::a, AddressingMode.ABSOLUTE, Operation.AND, 0x2D, "AND %04X", 4),
    EOR(Registers::a, AddressingMode.ABSOLUTE, Operation.EOR, 0x4D, "EOR %04X", 4),
    LDA(Registers::a, AddressingMode.ABSOLUTE, Operation.EQU, 0xAD, "LDA %04X", 4),
    LDX(Registers::x, AddressingMode.ABSOLUTE, Operation.EQU, 0xAE, "LDX %04X", 4),
    LDY(Registers::y, AddressingMode.ABSOLUTE, Operation.EQU, 0xAC, "LDY %04X", 4),
    ORA(Registers::a, AddressingMode.ABSOLUTE, Operation.ORA, 0x0D, "ORA %04X", 4),
    // Indexed
    ANDX(Registers::a, AddressingMode.INDEXED_X, Operation.AND, 0x3D, "AND $%04X,X", 4),
    ANDY(Registers::a, AddressingMode.INDEXED_Y, Operation.AND, 0x39, "AND $%04X,X", 4),
    LDAX(Registers::a, AddressingMode.INDEXED_X, Operation.EQU, 0xBD, "LDA $%04X,X", 4),
    LDAY(Registers::a, AddressingMode.INDEXED_Y, Operation.EQU, 0xB9, "LDA $%04X,Y", 4),
    LDXY(Registers::x, AddressingMode.INDEXED_Y, Operation.EQU, 0xBE, "LDX $%04X,Y", 4),
    LDYX(Registers::y, AddressingMode.INDEXED_X, Operation.EQU, 0xBC, "LDY $%04X,X", 4),
    EORX(Registers::a, AddressingMode.INDEXED_X, Operation.EOR, 0x5D, "EOR $%04X,X", 4),
    EORY(Registers::a, AddressingMode.INDEXED_Y, Operation.EOR, 0x59, "EOR $%04X,X", 4),
    ORAX(Registers::a, AddressingMode.INDEXED_X, Operation.ORA, 0x1D, "ORA $%04X,X", 4),
    ORAY(Registers::a, AddressingMode.INDEXED_Y, Operation.ORA, 0x19, "ORA $%04X,X", 4);


    private final int opcode;
    private final String mnemonic;
    private final BiConsumer<Registers, Integer> to;
    private final Operation operation;
    private final AddressingMode addressingMode;
    private final int cycles;

    Lda(BiConsumer<Registers,Integer> to, AddressingMode addressingMode, Operation operation,
        int opcode, String mnemonic, int cycles) {
        this.to = to;
        this.addressingMode = addressingMode;
        this.operation = operation;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
        this.cycles = cycles;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        int address = addressingMode.getEffectiveAddress(registers, peeker);

        to.accept(registers, operation.apply(registers,peeker.peek(address)));
        int cycles = this.cycles + addressingMode.getPenalty(registers, peeker);
        registers.incrementPc(2);
        return cycles;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format(mnemonic, parameter);
    }
}
