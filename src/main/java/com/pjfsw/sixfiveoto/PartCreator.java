package com.pjfsw.sixfiveoto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pjfsw.sixfiveoto.addressables.MemoryModule;
import com.pjfsw.sixfiveoto.addressables.via.Pin;
import com.pjfsw.sixfiveoto.addressables.via.Via6522;
import com.pjfsw.sixfiveoto.gameduino.Gameduino;
import com.pjfsw.sixfiveoto.lcd.Lcd;
import com.pjfsw.sixfiveoto.peripherals.Switch;
import com.pjfsw.sixfiveoto.picogfx.PicoGfx;
import com.pjfsw.sixfiveoto.serialrom.SerialRom;
import com.pjfsw.sixfiveoto.spi.Spi;

public final class PartCreator {
    private PartCreator() {
        // only static method
    }

    public static Part createPart(Config properties, String name, Map<String, Part> parts,
        Map<Integer, String> symbols)
        throws IOException, InterruptedException {
        String type = properties.getProperty(name, "");
        if (type.equalsIgnoreCase("rom")) {
            return createRom(properties,name, symbols);
        } else if (type.equalsIgnoreCase("ram")) {
            return createRam();
        } else if (type.equalsIgnoreCase("spi")) {
            return createSpi();
        } else if (type.equalsIgnoreCase("gameduino")) {
            return createGameduino(properties, name, parts);
        } else if (type.equalsIgnoreCase("serialrom")) {
            return createSerialRom(properties, name, parts);
        } else if (type.equalsIgnoreCase("switch")) {
            return createSwitch(properties, name);
        } else if (type.equalsIgnoreCase("via")) {
            return createVia(properties, name, parts);
        } else if (type.equalsIgnoreCase("lcd")) {
            return createLcd(properties, name);
        } else if (type.equalsIgnoreCase("picogfx")) {
            return createPicoGfx(properties);

        } else {
            throw new IllegalArgumentException(String.format("Unknown part type %s for %s", type, name));
        }
    }

    private static Part createRom(
        Config properties, String name,
        final Map<Integer, String> symbols
    )
        throws IOException, InterruptedException {
        String prg = properties.getProperty(name+".source", "");
        if (prg.toLowerCase().endsWith(".asm")) {
            prg = SourceLoader.compileSource(prg);
            if (prg == null) {
                System.err.println("Terminating because of compilation failure");
                System.exit(1);
            }
        }

        MemoryModule rom = MemoryModule.create8K();
        if (prg.toLowerCase().endsWith(".prg")) {
            byte[] bytes = Files.readAllBytes(new File(prg).toPath());
            int programBase = ((int)bytes[0]&0xff) + (((int)(bytes[1])&0xff) << 8);
            for (int i = 0; i < bytes.length-2; i++) {
                rom.poke(programBase+i, ((int)bytes[i+2]) & 0xFF);
            }
            symbols.putAll(SymbolMap.getSymbolsFromPrg(prg));
        }

        return Part.create(PartType.ROM, rom, null, null, null, null, null);
    }

    private static Part createRam() {
        MemoryModule ram = MemoryModule.create32K();
        return Part.create(PartType.RAM, ram, ram, null, null, null, null);
    }

    private static Part createSpi() {
        return Part.spiPart(new Spi());
    }

    private static int[] readDump(String filename) throws IOException {
        List<Integer> dump = new ArrayList<>();
        List<String> lines = Files.readAllLines(
            new File(filename).toPath());
        for (String line : lines) {
            for (String number : line.split(",")) {
                dump.add(Integer.parseInt(number.trim(), 16));
            }
        }

        return dump.stream().mapToInt(i->i).toArray();
    }

    private static Spi findSpi(Config properties, String name, Map<String, Part> parts) {
        String spiName = properties.getProperty(name+".spi", null);
        if (spiName == null) {
            throw new IllegalArgumentException(String.format("Must specify SPI part for %s", name));
        }
        Part part = parts.get(spiName);
        if (part == null || part.getSpi() == null) {
            throw new IllegalArgumentException(String.format("%s is not a valid SPI part", spiName));
        }
        return part.getSpi();
    }

    private static Part createPicoGfx(Config properties) throws IOException {
        int [] font = readDump("src/main/resources/dumps/font.txt");
        PicoGfx picoGfx = new PicoGfx(ClockspeedGetter.getClockSpeed(properties), font);
        return Part.create(PartType.PICOGFX, null, picoGfx, picoGfx, picoGfx, picoGfx, null);
    }

    private static Part createGameduino(Config properties, String name, Map<String, Part> parts)
        throws IOException {
        int[] dump = readDump("src/main/resources/dumps/gddump.txt");
        Spi spi = findSpi(properties, name, parts);
        Gameduino gameduino = new Gameduino(ClockspeedGetter.getClockSpeed(properties), spi, dump);
        return Part.create(PartType.GAMEDUINO, null, null, gameduino, gameduino, gameduino, null);
    }

    private static Part createSerialRom(Config properties, String name, Map<String, Part> parts)
        throws IOException, InterruptedException {
        Spi spi = findSpi(properties, name, parts);

        String prg = properties.getProperty(name+".source", "");

        if (prg.toLowerCase().endsWith(".asm")) {
            prg = SourceLoader.compileSource(prg);
            if (prg == null) {
                System.err.println("Terminating because of compilation failure");
                System.exit(1);
            }
        }
        int[] serialRomBytes = {};
        if (prg.toLowerCase().endsWith(".prg")) {
            byte[] bytes = Files.readAllBytes(new File(prg).toPath());
            serialRomBytes = new int[bytes.length-2];
            for (int i = 0; i < serialRomBytes.length; i++) {
                serialRomBytes[i] = ((int)bytes[i+2]) & 0xFF;
            }
        }

        SerialRom cartridge = new SerialRom(spi,  serialRomBytes);
        return Part.create(PartType.SERIALROM, null, null, cartridge, null, cartridge, null);
    }

    private static Part createSwitch(Config properties, String name) {
        boolean invert = Boolean.parseBoolean(properties.getProperty(name + ".invert", "false"));
        Switch aSwitch = invert ? Switch.inverted() : Switch.normal();
        return Part.createSwitch(aSwitch);
    }

    private static List<Pin> getPinsToConnect(
        Map<String, Part> parts,
        String port
    ) {
        List<Pin> pins = new ArrayList<>();
        String[] connections = port.split(",");
        for (String connection : connections) {
            String[] pin = connection.split(":");
            Part part = parts.get(pin[0]);
            if (part == null || part.getConnectable() == null) {
                throw new IllegalArgumentException(
                    String.format("Illegal part %s specified in pin connection %s", pin[0], connection));
            }

            Pin partPin = part.getConnectable().getPin(pin[pin.length-1 ]);
            if (partPin != null) {
                pins.add(partPin);
            } else {
                throw new IllegalArgumentException(String.format(
                    "Unknown pin '%s' in connection '%s'",
                    pin[1], connection));
            }
        }
        return pins;
    }

    private static Part createVia(Config properties, String name, Map<String, Part> parts) {
        Via6522 via = new Via6522();

        for (int i = 0; i < 8; i++) {
            String portA = properties.getProperty(name+".a"+i, null);
            if (portA != null) {
                List<Pin> pinsToConnect = getPinsToConnect(parts, portA);
                for (Pin pin : pinsToConnect) {
                    via.connectPortA(i, pin);
                }
            }
            String portB = properties.getProperty(name+".b"+i, null);
            if (portB != null) {
                List<Pin> pinsToConnect = getPinsToConnect(parts, portB);
                for (Pin pin : pinsToConnect) {
                    via.connectPortB(i, pin);
                }
            }
        }

        return Part.create(PartType.VIA, via, via, via, via, via, null);
    }

    private static Part createLcd(final Config properties, final String name) {
        int w = Integer.parseInt(properties.getProperty(name+".w", "16"));
        int h = Integer.parseInt(properties.getProperty(name+".h", "2"));

        Lcd lcd = new Lcd(ClockspeedGetter.getClockSpeed(properties), w, h);
        return Part.create(PartType.LCD, null, null, lcd, lcd, lcd, lcd);
    }
}

