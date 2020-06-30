package com.pjfsw.sixfiveoto.instruction;

import java.util.function.Function;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum StMemory implements Instruction {
    // Absolute
    STA(Registers::a, AddressingMode.ABSOLUTE, 0x8D, "STA $%04X", 4, 2),
    STX(Registers::x, AddressingMode.ABSOLUTE, 0x8E, "STX $%04X", 4, 2),
    STY(Registers::y, AddressingMode.ABSOLUTE, 0x8C, "STY $%04X", 4, 2),

    // Indexed
    STAX(Registers::a, AddressingMode.INDEXED_X, 0x9D, "STA $%04X,X", 5, 2),
    STAY(Registers::a, AddressingMode.INDEXED_Y, 0x99, "STA $%04X,Y", 5, 2),

    // Zeropage
    STAZ(Registers::a, AddressingMode.ZEROPAGE, 0x85, "STA $%02X", 3, 1),
    STXZ(Registers::x, AddressingMode.ZEROPAGE, 0x86, "STX $%02X", 3, 1),
    STYZ(Registers::y, AddressingMode.ZEROPAGE, 0x84, "STY $%02X", 3, 1),

    // Zeropage indexed
    STAZX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, 0x95, "STA $%02X,X", 4, 1),
    STXZY(Registers::x, AddressingMode.ZEROPAGE_INDEXED_Y, 0x96, "STX $%02X,Y", 4, 1),
    STYZX(Registers::y, AddressingMode.ZEROPAGE_INDEXED_X, 0x94, "STY $%02X,X", 4, 1),

    // Indexed indirect
    STAIX(Registers::a, AddressingMode.INDEXED_INDIRECT, 0x81, "STA ($%02X,X)", 6, 1),

    // Indirect indexed
    STAIY(Registers::a, AddressingMode.INDIRECT_INDEXED, 0x91, "STA ($%02X,X)", 6, 1);



    private final int opcode;
    private final String mnemonic;
    private final Function<Registers, Integer> from;
    private final AddressingMode addressingMode;
    private final int cycles;
    private final int paramLength;

    StMemory(Function<Registers,Integer> from, AddressingMode addressingMode, int opcode,
        String mnemonic, int cycles, int paramLength) {
        this.from = from;
        this.addressingMode = addressingMode;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
        this.cycles = cycles;
        this.paramLength = paramLength;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        int address = addressingMode.getEffectiveAddress(registers, peeker);
        poker.poke(address, from.apply(registers));
        registers.incrementPc(paramLength);
        return cycles;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format(mnemonic, parameter);
    }
}
