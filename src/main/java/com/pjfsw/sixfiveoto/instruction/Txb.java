package com.pjfsw.sixfiveoto.instruction;

import java.util.function.BiFunction;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum Txb implements Instruction {
    TRBZ(AddressingMode.ZEROPAGE, Txb::trb, 0x14, "TRB $%02X", 5),
    TRBA(AddressingMode.ABSOLUTE, Txb::trb, 0x1c, "TRB $%04X", 6),
    TSBZ(AddressingMode.ZEROPAGE, Txb::tsb, 0x04, "TSB $%02X", 5),
    TSBA(AddressingMode.ABSOLUTE, Txb::tsb, 0x0c, "TSB $%04X", 6)
    ;

    private final AddressingMode addressingMode;
    private final int cycles;
    private final int opcode;
    private final String mnemonic;
    private final BiFunction<Registers, Integer, Integer> operation;

    Txb(AddressingMode addressingMode, BiFunction<Registers, Integer, Integer> operation, int opcode, String mnemonic, int cycles) {
        this.addressingMode = addressingMode;
        this.operation = operation;
        this.cycles = cycles;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    private static Integer trb(Registers registers, Integer value) {
        return value & (registers.a() ^ 0xFF);
    }

    private static Integer tsb(Registers registers, Integer value) {
        return value | registers.a();
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        int address = addressingMode.getEffectiveAddress(registers, peeker);
        int value = peeker.peek(address);
        registers.z = (value & registers.a()) == 0 ;
        int result = operation.apply(registers, peeker.peek(address));
        poker.poke(address, result);
        registers.incrementPc(addressingMode.getParameterSize());

        return cycles;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format(mnemonic, parameter);
    }
}
