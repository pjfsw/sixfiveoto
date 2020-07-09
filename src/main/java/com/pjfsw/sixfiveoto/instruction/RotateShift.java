package com.pjfsw.sixfiveoto.instruction;

import java.util.function.BiFunction;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.mnemonicformatter.MnemonicFormatter;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum RotateShift implements Instruction {
    ASLI(AddressingMode.IMPLIED, Registers::asl, 0x0A, "ASL", 2),
    ASLZ(AddressingMode.ZEROPAGE, Registers::asl, 0x06, "ASL", 5),
    ASLZX(AddressingMode.ZEROPAGE_INDEXED_X, Registers::asl, 0x16, "ASL", 6),
    ASLA(AddressingMode.ABSOLUTE, Registers::asl, 0x0E, "ASL", 6),
    ASLAX(AddressingMode.INDEXED_X, Registers::asl, 0x1E, "ASL", 6),

    LSRI(AddressingMode.IMPLIED, Registers::lsr, 0x4A, "LSR", 2),
    LSRZ(AddressingMode.ZEROPAGE, Registers::lsr, 0x46, "LSR", 5),
    LSRZX(AddressingMode.ZEROPAGE_INDEXED_X, Registers::lsr, 0x56, "LSR", 6),
    LSRA(AddressingMode.ABSOLUTE, Registers::lsr, 0x4E, "LSR", 6),
    LSRAX(AddressingMode.INDEXED_X, Registers::lsr, 0x5E, "LSR", 6),

    ROLI(AddressingMode.IMPLIED, Registers::rol, 0x2A, "ROL", 2),
    ROLZ(AddressingMode.ZEROPAGE, Registers::rol, 0x26, "ROL", 5),
    ROLZX(AddressingMode.ZEROPAGE_INDEXED_X, Registers::rol, 0x36, "ROL", 6),
    ROLA(AddressingMode.ABSOLUTE, Registers::rol, 0x2E, "ROL", 6),
    ROLAX(AddressingMode.INDEXED_X, Registers::rol, 0x3E, "ROL", 6),

    RORI(AddressingMode.IMPLIED, Registers::ror, 0x6A, "ROR", 2),
    RORZ(AddressingMode.ZEROPAGE, Registers::ror, 0x66, "ROR", 5),
    RORZX(AddressingMode.ZEROPAGE_INDEXED_X, Registers::ror, 0x76, "ROR", 6),
    RORA(AddressingMode.ABSOLUTE, Registers::ror, 0x6E, "ROR", 6),
    RORAX(AddressingMode.INDEXED_X, Registers::ror, 0x7E, "ROR", 6);

    private final AddressingMode addressingMode;
    private final int opcode;
    private final String mnemonic;
    private final int cycles;
    private final BiFunction<Registers, Integer, Integer> operation;

    RotateShift(AddressingMode addressingMode, BiFunction<Registers,Integer,Integer> operation, int opcode, String mnemonic, int cycles) {
        this.addressingMode = addressingMode;
        this.operation = operation;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
        this.cycles = cycles;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        if (addressingMode == AddressingMode.IMPLIED) {
            registers.a(operation.apply(registers, registers.a()));
        } else {
            int address = addressingMode.getEffectiveAddress(registers, peeker);
            poker.poke(address, operation.apply(registers, peeker.peek(address)));
        }

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
