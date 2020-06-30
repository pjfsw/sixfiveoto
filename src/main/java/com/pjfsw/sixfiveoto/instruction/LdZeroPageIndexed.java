package com.pjfsw.sixfiveoto.instruction;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum LdZeroPageIndexed implements Instruction {
    ANDX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, Operation.AND, 0x35, "AND $%02X,X"),
    EORX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, Operation.EOR, 0x55, "EOR $%02X,X"),
    LDAX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, Operation.EQU, 0xB5, "LDA $%02X,X"),
    LDXY(Registers::x, AddressingMode.ZEROPAGE_INDEXED_Y, Operation.EQU, 0xB6, "LDX $%02X,Y"),
    LDYX(Registers::y, AddressingMode.ZEROPAGE_INDEXED_X, Operation.EQU, 0xB4, "LDY $%02X,X"),
    ORAX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, Operation.ORA, 0x15, "ORA $%02X,X");

    private final int opcode;
    private final String mnemonic;
    private final BiConsumer<Registers, Integer> to;
    private final Operation operation;
    private final AddressingMode addressingMode;

    LdZeroPageIndexed(
        BiConsumer<Registers,Integer> to,
        AddressingMode addressingMode,
        Operation operation, int opcode, String mnemonic) {
        this.to = to;
        this.addressingMode = addressingMode;
        this.operation = operation;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        int address = addressingMode.getEffectiveAddress(registers, peeker);
        to.accept(registers, operation.apply(registers,peeker.peek(address)));
        registers.incrementPc(1);
        return 4;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format(mnemonic, parameter);
    }
}
