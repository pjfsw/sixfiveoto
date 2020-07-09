package com.pjfsw.sixfiveoto;

import java.util.Map;

import com.pjfsw.sixfiveoto.instruction.Instruction;
import com.pjfsw.sixfiveoto.mnemonicformatter.MnemonicFormatter;

public class Disassembler {
    private final AddressDecoder addressDecoder;
    private final Map<Integer, Instruction> instructions;
    private final Map<Integer, String> symbols;
    private int pc;

    public Disassembler(
        AddressDecoder addressDecoder,
        Map<Integer,Instruction> instructions,
        Map<Integer,String> symbols,
        int pc
    ) {
        this.addressDecoder = addressDecoder;
        this.instructions = instructions;
        this.symbols = symbols;
        this.pc = pc;
    }

    public int getPc() {
        return pc;
    }

    public String disassemble() {
        int opcode = addressDecoder.peek(pc);
        Instruction instruction = instructions.get(opcode);
        String instructionAsText;
        if (instruction != null) {
            String mnemonic = instruction.getMnemonic();
            MnemonicFormatter formatter =
                instruction.getMnemonicFormatter();
            instructionAsText = formatter.format(
                mnemonic,pc+1,
                Memory.readWord(addressDecoder, pc+1),
                symbols
            );
            pc = Memory.add(pc, instruction.length());
        } else {
            instructionAsText = String.format("???(%02X)", opcode);
            pc = Memory.add(pc, 1);
        }
        return instructionAsText;
    }
}
