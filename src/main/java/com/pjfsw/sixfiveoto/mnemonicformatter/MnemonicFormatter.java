package com.pjfsw.sixfiveoto.mnemonicformatter;

import java.util.Map;

import com.pjfsw.sixfiveoto.Memory;

public enum MnemonicFormatter {
    IMPLIED((mnemonic,pc, argument, symbols)->
        mnemonic),
    IMMEDIATE((mnemonic, pc, argument, symbols)->
        String.format("%s #$%02X", mnemonic, argument & 0xFF)),
    ABSOLUTE((mnemonic, pc, address, symbols)->
        String.format("%s %s", mnemonic,
            symbols.getOrDefault(address, String.format("$%04X", address)))),
    INDEXED_X((mnemonic, pc, address, symbols)->
            String.format("%s %s", mnemonic,
                symbols.getOrDefault(address, String.format("$%04X,X", address)))),
    INDEXED_Y((mnemonic, pc, address, symbols)->
        String.format("%s %s", mnemonic,
            symbols.getOrDefault(address, String.format("$%04X,Y", address)))),
    ZEROPAGE((mnemonic, pc, address, symbols)->
        String.format("%s %s", mnemonic,
            symbols.getOrDefault(address, String.format("$%02X", address & 0xFF)))),
    ZEROPAGE_INDEXED_X((mnemonic, pc, address, symbols)->
        String.format("%s %s", mnemonic,
            symbols.getOrDefault(address, String.format("$%02X,X", address & 0xFF)))),
    ZEROPAGE_INDEXED_Y((mnemonic, pc, address, symbols)->
        String.format("%s %s", mnemonic,
            symbols.getOrDefault(address, String.format("$%02X,Y", address & 0xFF)))),
    INDEXED_INDIRECT((mnemonic, pc, address, symbols)->
        String.format("%s %s", mnemonic,
            symbols.getOrDefault(address, String.format("($%02X,X)", address & 0xFF)))),
    INDIRECT_INDEXED((mnemonic, pc, address, symbols)->
        String.format("%s %s", mnemonic,
            symbols.getOrDefault(address, String.format("($%02X),Y", address & 0xFF)))),
    INDIRECT((mnemonic, pc, address, symbols)->
        String.format("%s %s", mnemonic,
            symbols.getOrDefault(address, String.format("($%02X)", address & 0xFF)))),
    INDIRECT_ADDRESS((mnemonic, pc, address, symbols)->
        String.format("%s %s", mnemonic,
            symbols.getOrDefault(address, String.format("($%04X)", address)))),
    RELATIVE((mnemonic, pc, offset, symbols)->{
        int effectiveAddress = Memory.addSigned(pc+1, offset);
        return String.format("%s %s", mnemonic,
            symbols.getOrDefault(effectiveAddress, String.format("$%04X", effectiveAddress)));
    }),
    INDEXED_INDIRECT_ADDRESS((mnemonic, pc, address, symbols)->
        String.format("%s %s", mnemonic,
            symbols.getOrDefault(address, String.format("($%04X,X)", address))));

    private final Formatter formatter;

    MnemonicFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    public String format(String mnemonic, int pc, int argument, Map<Integer, String> symbolMap) {
        return formatter.format(mnemonic, pc, argument, symbolMap);
    }

    private interface Formatter {
        String format(String mnemonic, int pc, int argument, Map<Integer, String> symbolMap);
    }
}
