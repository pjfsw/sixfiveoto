package com.pjfsw.sixfiveoto.instruction;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.mnemonicformatter.MnemonicFormatter;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum Adc implements Instruction {
    ADCI (AluOperation.ADC, AddressingMode.IMMEDIATE, 0x69, "ADC", 2),
    ADC  (AluOperation.ADC, AddressingMode.ABSOLUTE, 0x6D, "ADC", 4),
    ADCX (AluOperation.ADC, AddressingMode.INDEXED_X, 0x7D, "ADC", 4),
    ADCY (AluOperation.ADC, AddressingMode.INDEXED_Y, 0x79, "ADC", 4),
    ADCZ (AluOperation.ADC, AddressingMode.ZEROPAGE,  0x65, "ADC", 3),
    ADCZX(AluOperation.ADC, AddressingMode.ZEROPAGE_INDEXED_X, 0x75, "ADC", 4),
    ADCIX(AluOperation.ADC, AddressingMode.INDEXED_INDIRECT, 0x61, "ADC", 6),
    ADCIY(AluOperation.ADC, AddressingMode.INDIRECT_INDEXED, 0x71, "ADC", 5),

    SBCI (AluOperation.SBC, AddressingMode.IMMEDIATE, 0xE9, "SBC", 2),
    SBC  (AluOperation.SBC, AddressingMode.ABSOLUTE, 0xED, "SBC", 4),
    SBCX (AluOperation.SBC, AddressingMode.INDEXED_X, 0xFD, "SBC", 4),
    SBCY (AluOperation.SBC, AddressingMode.INDEXED_Y, 0xF9, "SBC", 4),
    SBCZ (AluOperation.SBC, AddressingMode.ZEROPAGE,  0xE5, "SBC", 3),
    SBCZX(AluOperation.SBC, AddressingMode.ZEROPAGE_INDEXED_X, 0xF5, "SBC", 4),
    SBCIX(AluOperation.SBC, AddressingMode.INDEXED_INDIRECT, 0xE1, "SBC", 6),
    SBCIY(AluOperation.SBC, AddressingMode.INDIRECT_INDEXED, 0xF1, "SBC", 5),

    // 65C02 specific
    ADCZI(AluOperation.ADC, AddressingMode.INDIRECT, 0x72, "ADC", 5),
    SBCZI(AluOperation.SBC, AddressingMode.INDIRECT, 0xF2, "SBC", 5);

    private final AddressingMode addressingMode;
    private final AluOperation aluOperation;
    private final int opcode;
    private final String mnemonic;
    private final int cycles;

    Adc(AluOperation aluOperation, AddressingMode addressingMode,
        int opcode, String mnemonic, int cycles) {
        this.aluOperation = aluOperation;
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
        registers.a(
            aluOperation.calculate(registers).apply(
                registers.a(), peeker.peek(address)
            )
        );
        int penalty = addressingMode.getPenalty(registers, peeker);
        registers.incrementPc(addressingMode.getParameterSize());
        return cycles + penalty;
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
