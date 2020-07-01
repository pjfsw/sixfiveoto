package com.pjfsw.sixfiveoto.instruction;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum Adc implements Instruction {
    ADCI (AluOperation.ADC, AddressingMode.IMMEDIATE, 0x69, "ADC #$%02X", 2, 1),
    ADC  (AluOperation.ADC, AddressingMode.ABSOLUTE, 0x6D, "ADC $%04X", 4, 2),
    ADCX (AluOperation.ADC, AddressingMode.INDEXED_X, 0x7D, "ADC $%04X,X", 4, 2),
    ADCY (AluOperation.ADC, AddressingMode.INDEXED_Y, 0x79, "ADC $%04X,Y", 4, 2),
    ADCZ (AluOperation.ADC, AddressingMode.ZEROPAGE,  0x65, "ADC $%02X", 3, 1),
    ADCZX(AluOperation.ADC, AddressingMode.ZEROPAGE_INDEXED_X, 0x75, "ADC $%02X,X", 4,1),
    ADCIX(AluOperation.ADC, AddressingMode.INDEXED_INDIRECT, 0x61, "ADC ($%02X,X)", 6,1),
    ADCIY(AluOperation.ADC, AddressingMode.INDIRECT_INDEXED, 0x71, "ADC ($%02X),Y", 5,1),

    SBCI (AluOperation.SBC, AddressingMode.IMMEDIATE, 0xE9, "SBC #$%02X", 2, 1),
    SBC  (AluOperation.SBC, AddressingMode.ABSOLUTE, 0xED, "SBC $%04X", 4, 2),
    SBCX (AluOperation.SBC, AddressingMode.INDEXED_X, 0xFD, "SBC $%04X,X", 4, 2),
    SBCY (AluOperation.SBC, AddressingMode.INDEXED_Y, 0xF9, "SBC $%04X,Y", 4, 2),
    SBCZ (AluOperation.SBC, AddressingMode.ZEROPAGE,  0xE5, "SBC $%02X", 3, 1),
    SBCZX(AluOperation.SBC, AddressingMode.ZEROPAGE_INDEXED_X, 0xF5, "SBC $%02X,X", 4,1),
    SBCIX(AluOperation.SBC, AddressingMode.INDEXED_INDIRECT, 0xE1, "SBC ($%02X,X)", 6,1),
    SBCIY(AluOperation.SBC, AddressingMode.INDIRECT_INDEXED, 0xF1, "SBC ($%02X),Y", 5,1);

    private final AddressingMode addressingMode;
    private final AluOperation aluOperation;
    private final int opcode;
    private final String mnemonic;
    private final int cycles;
    private final int paramSize;

    Adc(AluOperation aluOperation, AddressingMode addressingMode,
        int opcode, String mnemonic, int cycles, int paramSize) {
        this.aluOperation = aluOperation;
        this.addressingMode = addressingMode;
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
        registers.a(
            aluOperation.calculate(registers).apply(
                registers.a(), peeker.peek(address)
            )
        );
        int penalty = addressingMode.getPenalty(registers, peeker);
        registers.incrementPc(paramSize);
        return cycles + penalty;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return mnemonic;
    }
}
