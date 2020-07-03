package com.pjfsw.sixfiveoto.instruction;

import java.util.function.Function;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum Cmp implements Instruction {
    CMPI(Registers::a, AddressingMode.IMMEDIATE, 0xC9, "CMP #$%02X", 2),
    CMPZ(Registers::a, AddressingMode.ZEROPAGE, 0xC5, "CMP $%02X", 3),
    CMPZX(Registers::a, AddressingMode.ZEROPAGE_INDEXED_X, 0xD5, "CMP $%02X,X", 4),
    CMPA(Registers::a, AddressingMode.ABSOLUTE, 0xCD, "CMP $%04", 4),
    CMPAX(Registers::a, AddressingMode.INDEXED_X, 0xDD, "CMP $%04,X", 4),
    CMPAY(Registers::a, AddressingMode.INDEXED_Y, 0xD9, "CMP $%04,Y", 4),
    CMPIX(Registers::a, AddressingMode.INDEXED_INDIRECT, 0xC1, "CMP ($%02,X)", 6),
    CMPIY(Registers::a, AddressingMode.INDIRECT_INDEXED, 0xD1, "CMP ($%02),Y", 5),
    CPXI(Registers::x, AddressingMode.IMMEDIATE, 0xE0,  "CPX #$%02X", 2),
    CPXZ(Registers::x, AddressingMode.ZEROPAGE, 0xE4, "CPX $%02X", 3),
    CPXA(Registers::x, AddressingMode.ABSOLUTE, 0xEC, "CPX $%04X", 4),
    CPYI(Registers::y, AddressingMode.IMMEDIATE, 0xC0,  "CPY #$%02X", 2),
    CPYZ(Registers::y, AddressingMode.ZEROPAGE, 0xC4, "CPY $%02X", 3),
    CPYA(Registers::y, AddressingMode.ABSOLUTE, 0xCC, "CPY $%04X", 4)
    ;

    private final Function<Registers, Integer> comparand;
    private final AddressingMode addressingMode;
    private final int opcode;
    private final String mnemonic;
    private final int cycles;

    Cmp(Function<Registers,Integer> comparand, AddressingMode addressingMode,
        int opcode, String mnemonic, int cycles) {

        this.comparand = comparand;
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
        registers.incrementPc(addressingMode.getParameterSize());
        registers.c = true;
        registers.sbc(comparand.apply(registers), peeker.peek(address));
        return cycles + addressingMode.getPenalty(registers, peeker);
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format(mnemonic, parameter);
    }
}
