package com.pjfsw.sixfiveoto.instruction;

import java.util.function.BiConsumer;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum LdMemory implements Instruction {
    // Absolute
    AND(Registers::a, AddressingMode.ABSOLUTE, Operation.AND, 0x2D, "AND $%04X", 4, 2),
    EOR(Registers::a, AddressingMode.ABSOLUTE, Operation.EOR, 0x4D, "EOR $%04X", 4, 2),
    LDA(Registers::a, AddressingMode.ABSOLUTE, Operation.EQU, 0xAD, "LDA $%04X", 4, 2),
    LDX(Registers::x, AddressingMode.ABSOLUTE, Operation.EQU, 0xAE, "LDX $%04X", 4, 2),
    LDY(Registers::y, AddressingMode.ABSOLUTE, Operation.EQU, 0xAC, "LDY $%04X", 4, 2),
    ORA(Registers::a, AddressingMode.ABSOLUTE, Operation.ORA, 0x0D, "ORA $%04X", 4, 2),
    // Indexed
    ANDX(Registers::a, AddressingMode.INDEXED_X, Operation.AND, 0x3D, "AND $%04X,X", 4, 2),
    ANDY(Registers::a, AddressingMode.INDEXED_Y, Operation.AND, 0x39, "AND $%04X,X", 4, 2),
    LDAX(Registers::a, AddressingMode.INDEXED_X, Operation.EQU, 0xBD, "LDA $%04X,X", 4, 2),
    LDAY(Registers::a, AddressingMode.INDEXED_Y, Operation.EQU, 0xB9, "LDA $%04X,Y", 4, 2),
    LDXY(Registers::x, AddressingMode.INDEXED_Y, Operation.EQU, 0xBE, "LDX $%04X,Y", 4, 2),
    LDYX(Registers::y, AddressingMode.INDEXED_X, Operation.EQU, 0xBC, "LDY $%04X,X", 4, 2),
    EORX(Registers::a, AddressingMode.INDEXED_X, Operation.EOR, 0x5D, "EOR $%04X,X", 4, 2),
    EORY(Registers::a, AddressingMode.INDEXED_Y, Operation.EOR, 0x59, "EOR $%04X,X", 4, 2),
    ORAX(Registers::a, AddressingMode.INDEXED_X, Operation.ORA, 0x1D, "ORA $%04X,X", 4, 2),
    ORAY(Registers::a, AddressingMode.INDEXED_Y, Operation.ORA, 0x19, "ORA $%04X,X", 4, 2),

    //  Zero page
    ANDZ(Registers::a, AddressingMode.ZEROPAGE, Operation.AND, 0x25, "AND $%02X", 3, 1),
    EORZ(Registers::a, AddressingMode.ZEROPAGE, Operation.EOR, 0x45, "EOR $%02X", 3, 1),
    LDAZ(Registers::a, AddressingMode.ZEROPAGE, Operation.EQU, 0xA5, "LDA $%02X", 3, 1),
    LDXZ(Registers::x, AddressingMode.ZEROPAGE, Operation.EQU, 0xA6, "LDX $%02X", 3, 1),
    LDYZ(Registers::y, AddressingMode.ZEROPAGE, Operation.EQU, 0xA4, "LDY $%02X", 3, 1),
    ORAZ(Registers::a, AddressingMode.ZEROPAGE, Operation.ORA, 0x05, "ORA $%02X", 3, 1),

    // Zero page indexed
    ANDZX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, Operation.AND, 0x35, "AND $%02X,X", 4, 1),
    EORZX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, Operation.EOR, 0x55, "EOR $%02X,X", 4, 1),
    LDAZX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, Operation.EQU, 0xB5, "LDA $%02X,X", 4, 1),
    LDXZY(Registers::x, AddressingMode.ZEROPAGE_INDEXED_Y, Operation.EQU, 0xB6, "LDX $%02X,Y", 4, 1),
    LDYZX(Registers::y, AddressingMode.ZEROPAGE_INDEXED_X, Operation.EQU, 0xB4, "LDY $%02X,X", 4, 1),
    ORAZX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, Operation.ORA, 0x15, "ORA $%02X,X", 4, 1),

    // Indexed indirect
    ANDIX(Registers::a, AddressingMode.INDEXED_INDIRECT, Operation.AND, 0x21, "AND ($%02X,X)", 6, 1),
    EORIX(Registers::a, AddressingMode.INDEXED_INDIRECT, Operation.EOR, 0x41, "EOR ($%02X,X)", 6, 1),
    LDAIX(Registers::a, AddressingMode.INDEXED_INDIRECT, Operation.EQU, 0xA1, "LDA ($%02X,X)", 6, 1),
    ORAIX(Registers::a, AddressingMode.INDEXED_INDIRECT, Operation.ORA, 0x01, "ORA ($%02X,X)", 6, 1),

    // Indirect indexed
    ANDIY(Registers::a, AddressingMode.INDIRECT_INDEXED, Operation.AND, 0x31, "AND ($%02X),Y", 5, 1),
    EORIY(Registers::a, AddressingMode.INDIRECT_INDEXED, Operation.EOR, 0x51, "EOR ($%02X),Y", 5, 1),
    LDAIY(Registers::a, AddressingMode.INDIRECT_INDEXED, Operation.EQU, 0xB1, "LDA ($%02X),Y", 5, 1),
    ORAIY(Registers::a, AddressingMode.INDIRECT_INDEXED, Operation.ORA, 0x11, "ORA ($%02X),Y", 5, 1);


    private final int opcode;
    private final String mnemonic;
    private final BiConsumer<Registers, Integer> to;
    private final Operation operation;
    private final AddressingMode addressingMode;
    private final int cycles;
    private final int paramSize;

    LdMemory(BiConsumer<Registers,Integer> to, AddressingMode addressingMode, Operation operation,
        int opcode, String mnemonic, int cycles, int paramSize) {
        this.to = to;
        this.addressingMode = addressingMode;
        this.operation = operation;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
        this.cycles = cycles;
        this.paramSize = paramSize;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        int address = addressingMode.getEffectiveAddress(registers, peeker);

        to.accept(registers, operation.apply(registers,peeker.peek(address)));
        int cycles = this.cycles + addressingMode.getPenalty(registers, peeker);
        registers.incrementPc(paramSize);
        return cycles;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format(mnemonic, parameter);
    }
}
