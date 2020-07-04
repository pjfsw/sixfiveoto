package com.pjfsw.sixfiveoto.instruction;

import java.util.function.Function;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum StMemory implements Instruction {
    // Absolute
    STA(Registers::a, AddressingMode.ABSOLUTE, 0x8D, "STA $%04X", 4),
    STX(Registers::x, AddressingMode.ABSOLUTE, 0x8E, "STX $%04X", 4),
    STY(Registers::y, AddressingMode.ABSOLUTE, 0x8C, "STY $%04X", 4),

    // Indexed
    STAX(Registers::a, AddressingMode.INDEXED_X, 0x9D, "STA $%04X,X", 5),
    STAY(Registers::a, AddressingMode.INDEXED_Y, 0x99, "STA $%04X,Y", 5),

    // Zeropage
    STAZ(Registers::a, AddressingMode.ZEROPAGE, 0x85, "STA $%02X", 3),
    STXZ(Registers::x, AddressingMode.ZEROPAGE, 0x86, "STX $%02X", 3),
    STYZ(Registers::y, AddressingMode.ZEROPAGE, 0x84, "STY $%02X", 3),

    // Zeropage indexed
    STAZX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, 0x95, "STA $%02X,X", 4),
    STXZY(Registers::x, AddressingMode.ZEROPAGE_INDEXED_Y, 0x96, "STX $%02X,Y", 4),
    STYZX(Registers::y, AddressingMode.ZEROPAGE_INDEXED_X, 0x94, "STY $%02X,X", 4),

    // Indexed indirect
    STAIX(Registers::a, AddressingMode.INDEXED_INDIRECT, 0x81, "STA ($%02X,X)", 6),

    // Indirect indexed
    STAIY(Registers::a, AddressingMode.INDIRECT_INDEXED, 0x91, "STA ($%02X,X)", 6),

    // 65C02 opcodes
    STZZ((reg) -> 0, AddressingMode.ZEROPAGE, 0x64, "STZ $%02X", 3),
    STZZX((reg) -> 0, AddressingMode.ZEROPAGE_INDEXED_X, 0x74, "STZ $%02X,X", 4),
    STZ((reg) -> 0, AddressingMode.ABSOLUTE, 0x9C, "STZ $%04X", 4),
    STZX((reg) -> 0, AddressingMode.INDEXED_X, 0x9E, "STZ $%04X,X", 5);




    private final int opcode;
    private final String mnemonic;
    private final Function<Registers, Integer> from;
    private final AddressingMode addressingMode;
    private final int cycles;

    StMemory(Function<Registers,Integer> from, AddressingMode addressingMode, int opcode,
        String mnemonic, int cycles) {
        this.from = from;
        this.addressingMode = addressingMode;
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
        poker.poke(address, from.apply(registers));
        registers.incrementPc(addressingMode.getParameterSize());
        return cycles;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format(mnemonic, parameter);
    }
}
