package com.pjfsw.sixfiveoto.instruction;

import java.util.function.BiConsumer;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum LdMemory implements Instruction {
    // Absolute
    AND(Registers::a, AddressingMode.ABSOLUTE, LoadOperation.AND, 0x2D, "AND $%04X", 4),
    EOR(Registers::a, AddressingMode.ABSOLUTE, LoadOperation.EOR, 0x4D, "EOR $%04X", 4),
    LDA(Registers::a, AddressingMode.ABSOLUTE, LoadOperation.LD, 0xAD, "LDA $%04X", 4),
    LDX(Registers::x, AddressingMode.ABSOLUTE, LoadOperation.LD, 0xAE, "LDX $%04X", 4),
    LDY(Registers::y, AddressingMode.ABSOLUTE, LoadOperation.LD, 0xAC, "LDY $%04X", 4),
    ORA(Registers::a, AddressingMode.ABSOLUTE, LoadOperation.ORA, 0x0D, "ORA $%04X", 4),
    // Indexed
    ANDX(Registers::a, AddressingMode.INDEXED_X, LoadOperation.AND, 0x3D, "AND $%04X,X", 4),
    ANDY(Registers::a, AddressingMode.INDEXED_Y, LoadOperation.AND, 0x39, "AND $%04X,X", 4),
    LDAX(Registers::a, AddressingMode.INDEXED_X, LoadOperation.LD, 0xBD, "LDA $%04X,X", 4),
    LDAY(Registers::a, AddressingMode.INDEXED_Y, LoadOperation.LD, 0xB9, "LDA $%04X,Y", 4),
    LDXY(Registers::x, AddressingMode.INDEXED_Y, LoadOperation.LD, 0xBE, "LDX $%04X,Y", 4),
    LDYX(Registers::y, AddressingMode.INDEXED_X, LoadOperation.LD, 0xBC, "LDY $%04X,X", 4),
    EORX(Registers::a, AddressingMode.INDEXED_X, LoadOperation.EOR, 0x5D, "EOR $%04X,X", 4),
    EORY(Registers::a, AddressingMode.INDEXED_Y, LoadOperation.EOR, 0x59, "EOR $%04X,X", 4),
    ORAX(Registers::a, AddressingMode.INDEXED_X, LoadOperation.ORA, 0x1D, "ORA $%04X,X", 4),
    ORAY(Registers::a, AddressingMode.INDEXED_Y, LoadOperation.ORA, 0x19, "ORA $%04X,X", 4),

    //  Zero page
    ANDZ(Registers::a, AddressingMode.ZEROPAGE, LoadOperation.AND, 0x25, "AND $%02X", 3),
    EORZ(Registers::a, AddressingMode.ZEROPAGE, LoadOperation.EOR, 0x45, "EOR $%02X", 3),
    LDAZ(Registers::a, AddressingMode.ZEROPAGE, LoadOperation.LD, 0xA5, "LDA $%02X", 3),
    LDXZ(Registers::x, AddressingMode.ZEROPAGE, LoadOperation.LD, 0xA6, "LDX $%02X", 3),
    LDYZ(Registers::y, AddressingMode.ZEROPAGE, LoadOperation.LD, 0xA4, "LDY $%02X", 3),
    ORAZ(Registers::a, AddressingMode.ZEROPAGE, LoadOperation.ORA, 0x05, "ORA $%02X", 3),

    // Zero page indexed
    ANDZX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, LoadOperation.AND, 0x35, "AND $%02X,X", 4),
    EORZX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, LoadOperation.EOR, 0x55, "EOR $%02X,X", 4),
    LDAZX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, LoadOperation.LD, 0xB5, "LDA $%02X,X", 4),
    LDXZY(Registers::x, AddressingMode.ZEROPAGE_INDEXED_Y, LoadOperation.LD, 0xB6, "LDX $%02X,Y", 4),
    LDYZX(Registers::y, AddressingMode.ZEROPAGE_INDEXED_X, LoadOperation.LD, 0xB4, "LDY $%02X,X", 4),
    ORAZX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, LoadOperation.ORA, 0x15, "ORA $%02X,X", 4),

    // Indexed indirect
    ANDIX(Registers::a, AddressingMode.INDEXED_INDIRECT, LoadOperation.AND, 0x21, "AND ($%02X,X)", 6),
    EORIX(Registers::a, AddressingMode.INDEXED_INDIRECT, LoadOperation.EOR, 0x41, "EOR ($%02X,X)", 6),
    LDAIX(Registers::a, AddressingMode.INDEXED_INDIRECT, LoadOperation.LD, 0xA1, "LDA ($%02X,X)", 6),
    ORAIX(Registers::a, AddressingMode.INDEXED_INDIRECT, LoadOperation.ORA, 0x01, "ORA ($%02X,X)", 6),

    // Indirect indexed
    ANDIY(Registers::a, AddressingMode.INDIRECT_INDEXED, LoadOperation.AND, 0x31, "AND ($%02X),Y", 5),
    EORIY(Registers::a, AddressingMode.INDIRECT_INDEXED, LoadOperation.EOR, 0x51, "EOR ($%02X),Y", 5),
    LDAIY(Registers::a, AddressingMode.INDIRECT_INDEXED, LoadOperation.LD, 0xB1, "LDA ($%02X),Y", 5),
    ORAIY(Registers::a, AddressingMode.INDIRECT_INDEXED, LoadOperation.ORA, 0x11, "ORA ($%02X),Y", 5);


    private final int opcode;
    private final String mnemonic;
    private final BiConsumer<Registers, Integer> to;
    private final LoadOperation loadOperation;
    private final AddressingMode addressingMode;
    private final int cycles;

    LdMemory(BiConsumer<Registers,Integer> to, AddressingMode addressingMode, LoadOperation loadOperation,
        int opcode, String mnemonic, int cycles) {
        this.to = to;
        this.addressingMode = addressingMode;
        this.loadOperation = loadOperation;
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

        to.accept(registers, loadOperation.apply(registers,peeker.peek(address)));
        int cycles = this.cycles + addressingMode.getPenalty(registers, peeker);
        registers.incrementPc(addressingMode.getParameterSize());
        return cycles;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format(mnemonic, parameter);
    }
}