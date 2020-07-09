package com.pjfsw.sixfiveoto.instruction;

import java.util.function.BiConsumer;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.mnemonicformatter.MnemonicFormatter;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum LdMemory implements Instruction {
    // Absolute
    AND(Registers::a, AddressingMode.ABSOLUTE, LoadOperation.AND, 0x2D, "AND", 4),
    EOR(Registers::a, AddressingMode.ABSOLUTE, LoadOperation.EOR, 0x4D, "EOR", 4),
    LDA(Registers::a, AddressingMode.ABSOLUTE, LoadOperation.LD, 0xAD, "LDA", 4),
    LDX(Registers::x, AddressingMode.ABSOLUTE, LoadOperation.LD, 0xAE, "LDX", 4),
    LDY(Registers::y, AddressingMode.ABSOLUTE, LoadOperation.LD, 0xAC, "LDY", 4),
    ORA(Registers::a, AddressingMode.ABSOLUTE, LoadOperation.ORA, 0x0D, "ORA", 4),
    // Indexed
    ANDX(Registers::a, AddressingMode.INDEXED_X, LoadOperation.AND, 0x3D, "AND", 4),
    ANDY(Registers::a, AddressingMode.INDEXED_Y, LoadOperation.AND, 0x39, "AND", 4),
    LDAX(Registers::a, AddressingMode.INDEXED_X, LoadOperation.LD, 0xBD, "LDA", 4),
    LDAY(Registers::a, AddressingMode.INDEXED_Y, LoadOperation.LD, 0xB9, "LDA", 4),
    LDXY(Registers::x, AddressingMode.INDEXED_Y, LoadOperation.LD, 0xBE, "LDX", 4),
    LDYX(Registers::y, AddressingMode.INDEXED_X, LoadOperation.LD, 0xBC, "LDY", 4),
    EORX(Registers::a, AddressingMode.INDEXED_X, LoadOperation.EOR, 0x5D, "EOR", 4),
    EORY(Registers::a, AddressingMode.INDEXED_Y, LoadOperation.EOR, 0x59, "EOR", 4),
    ORAX(Registers::a, AddressingMode.INDEXED_X, LoadOperation.ORA, 0x1D, "ORA", 4),
    ORAY(Registers::a, AddressingMode.INDEXED_Y, LoadOperation.ORA, 0x19, "ORA", 4),

    //  Zero page
    ANDZ(Registers::a, AddressingMode.ZEROPAGE, LoadOperation.AND, 0x25, "AND", 3),
    EORZ(Registers::a, AddressingMode.ZEROPAGE, LoadOperation.EOR, 0x45, "EOR", 3),
    LDAZ(Registers::a, AddressingMode.ZEROPAGE, LoadOperation.LD, 0xA5, "LDA", 3),
    LDXZ(Registers::x, AddressingMode.ZEROPAGE, LoadOperation.LD, 0xA6, "LDX", 3),
    LDYZ(Registers::y, AddressingMode.ZEROPAGE, LoadOperation.LD, 0xA4, "LDY", 3),
    ORAZ(Registers::a, AddressingMode.ZEROPAGE, LoadOperation.ORA, 0x05, "ORA", 3),

    // Zero page indexed
    ANDZX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, LoadOperation.AND, 0x35, "AND", 4),
    EORZX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, LoadOperation.EOR, 0x55, "EOR", 4),
    LDAZX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, LoadOperation.LD, 0xB5, "LDA", 4),
    LDXZY(Registers::x, AddressingMode.ZEROPAGE_INDEXED_Y, LoadOperation.LD, 0xB6, "LDX", 4),
    LDYZX(Registers::y, AddressingMode.ZEROPAGE_INDEXED_X, LoadOperation.LD, 0xB4, "LDY", 4),
    ORAZX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, LoadOperation.ORA, 0x15, "ORA", 4),

    // Indexed indirect
    ANDIX(Registers::a, AddressingMode.INDEXED_INDIRECT, LoadOperation.AND, 0x21, "AND", 6),
    EORIX(Registers::a, AddressingMode.INDEXED_INDIRECT, LoadOperation.EOR, 0x41, "EOR", 6),
    LDAIX(Registers::a, AddressingMode.INDEXED_INDIRECT, LoadOperation.LD, 0xA1, "LDA", 6),
    ORAIX(Registers::a, AddressingMode.INDEXED_INDIRECT, LoadOperation.ORA, 0x01, "ORA", 6),

    // Indirect indexed
    ANDIY(Registers::a, AddressingMode.INDIRECT_INDEXED, LoadOperation.AND, 0x31, "AND", 5),
    EORIY(Registers::a, AddressingMode.INDIRECT_INDEXED, LoadOperation.EOR, 0x51, "EOR", 5),
    LDAIY(Registers::a, AddressingMode.INDIRECT_INDEXED, LoadOperation.LD, 0xB1, "LDA", 5),
    ORAIY(Registers::a, AddressingMode.INDIRECT_INDEXED, LoadOperation.ORA, 0x11, "ORA", 5),

    // 65C02 Indirect
    ANDZI(Registers::a, AddressingMode.INDIRECT, LoadOperation.AND, 0x32, "AND", 5),
    EORZI(Registers::a, AddressingMode.INDIRECT, LoadOperation.EOR, 0x52, "EOR", 5),
    LDAZI(Registers::a, AddressingMode.INDIRECT, LoadOperation.LD,  0xB2, "LDA", 5),
    ORAZI(Registers::a, AddressingMode.INDIRECT, LoadOperation.ORA, 0x12, "ORA", 5);


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
    public String getMnemonic() {
        return mnemonic;
    }

    @Override
    public MnemonicFormatter getMnemonicFormatter() {
        return addressingMode.getFormatter();
    }
}
