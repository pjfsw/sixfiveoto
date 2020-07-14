package com.pjfsw.sixfiveoto.instruction;

import java.util.function.Function;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.mnemonicformatter.MnemonicFormatter;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum StMemory implements Instruction {
    // Absolute
    STA(Registers::a, AddressingMode.ABSOLUTE, 0x8D, "STA", 4),
    STX(Registers::x, AddressingMode.ABSOLUTE, 0x8E, "STX", 4),
    STY(Registers::y, AddressingMode.ABSOLUTE, 0x8C, "STY", 4),

    // Indexed
    STAX(Registers::a, AddressingMode.INDEXED_X, 0x9D, "STA", 5),
    STAY(Registers::a, AddressingMode.INDEXED_Y, 0x99, "STA", 5),

    // Zeropage
    STAZ(Registers::a, AddressingMode.ZEROPAGE, 0x85, "STA", 3),
    STXZ(Registers::x, AddressingMode.ZEROPAGE, 0x86, "STX", 3),
    STYZ(Registers::y, AddressingMode.ZEROPAGE, 0x84, "STY", 3),

    // Zeropage indexed
    STAZX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, 0x95, "STA", 4),
    STXZY(Registers::x, AddressingMode.ZEROPAGE_INDEXED_Y, 0x96, "STX", 4),
    STYZX(Registers::y, AddressingMode.ZEROPAGE_INDEXED_X, 0x94, "STY", 4),

    // Indexed indirect
    STAIX(Registers::a, AddressingMode.INDEXED_INDIRECT, 0x81, "STA", 6),

    // Indirect indexed
    STAIY(Registers::a, AddressingMode.INDIRECT_INDEXED, 0x91, "STA", 6),

    // 65C02 opcodes
    STAZI(Registers::a, AddressingMode.INDIRECT, 0x92, "STA", 5),
    STZZ((reg) -> 0, AddressingMode.ZEROPAGE, 0x64, "STZ", 3),
    STZZX((reg) -> 0, AddressingMode.ZEROPAGE_INDEXED_X, 0x74, "STZ", 4),
    STZ((reg) -> 0, AddressingMode.ABSOLUTE, 0x9C, "STZ", 4),
    STZX((reg) -> 0, AddressingMode.INDEXED_X, 0x9E, "STZ", 5);




    private final int opcode;
    private final String mnemonic;
    private final FromRegister from;
    private final AddressingMode addressingMode;
    private final int cycles;

    StMemory(FromRegister from, AddressingMode addressingMode, int opcode,
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
        poker.poke(address, from.load(registers));
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

    @Override
    public int length() {
        return addressingMode.getParameterSize()+1;
    }

}
