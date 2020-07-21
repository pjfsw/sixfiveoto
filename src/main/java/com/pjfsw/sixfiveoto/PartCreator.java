package com.pjfsw.sixfiveoto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.pjfsw.sixfiveoto.addressables.MemoryModule;
import com.pjfsw.sixfiveoto.addressables.via.Pin;
import com.pjfsw.sixfiveoto.addressables.via.Via6522;
import com.pjfsw.sixfiveoto.gameduino.Gameduino;
import com.pjfsw.sixfiveoto.peripherals.Switch;
import com.pjfsw.sixfiveoto.serialrom.SerialRom;
import com.pjfsw.sixfiveoto.spi.Spi;

public final class PartCreator {
    private PartCreator() {
        // only static method
    }

    public static Part createPart(Properties properties, String name, Map<String, Part> parts)
        throws IOException, InterruptedException {
        String type = properties.getProperty(name, "");
        if (type.equalsIgnoreCase("rom")) {
            return createRom(properties,name);
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
        } else {
            throw new IllegalArgumentException(String.format("Unknown part type %s for %s", type, name));
        }
    }

    private static Part createRom(Properties properties, String name)
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
        }
        return Part.create(rom, null, null, null, null);
    }

    private static Part createRam() {
        MemoryModule ram = MemoryModule.create32K();
        return Part.create(ram, ram, null, null, null);
    }

    private static Part createSpi() {
        return Part.spiPart(new Spi());
    }

    private static int[] readDump() throws IOException {
        List<Integer> dump = new ArrayList<>();
        List<String> lines = Files.readAllLines(
            new File("src/main/resources/dumps/gddump.txt").toPath());
        for (String line : lines) {
            for (String number : line.split(",")) {
                dump.add(Integer.parseInt(number.trim(), 16));
            }
        }

        return dump.stream().mapToInt(i->i).toArray();
    }

    private static Spi findSpi(Properties properties, String name, Map<String, Part> parts) {
        String spiName = properties.getProperty(name+".spi");
        if (spiName == null) {
            throw new IllegalArgumentException(String.format("Must specify SPI part for %s", name));
        }
        Part part = parts.get(spiName);
        if (part == null || part.getSpi() == null) {
            throw new IllegalArgumentException(String.format("%s is not a valid SPI part", spiName));
        }
        return part.getSpi();
    }

    private static Part createGameduino(Properties properties, String name, Map<String, Part> parts)
        throws IOException {
        int[] dump = readDump();
        Spi spi = findSpi(properties, name, parts);
        Gameduino gameduino = new Gameduino(ClockspeedGetter.getClockSpeed(properties), spi, dump);
        return Part.create(null, null, gameduino, gameduino, gameduino);
    }

    private static Part createSerialRom(Properties properties, String name, Map<String, Part> parts)
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
        return Part.create(null, null, cartridge, null, cartridge);
    }

    private static Part createSwitch(Properties properties, String name) {
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
            if (part == null) {
                throw new IllegalArgumentException(
                    String.format("Illegal part %s specified in pin connection %s", pin[0], connection));
            }
            if (part.getSwitch() != null) {
                pins.add(part.getSwitch().getPin());
            } else if (part.getSpi() != null && pin.length == 2) {
                if (pin[1].equalsIgnoreCase("clock")) {
                    pins.add(part.getSpi().getClock());
                } else if (pin[1].equalsIgnoreCase("slaveout")) {
                    pins.add(part.getSpi().getSlaveOut());
                } else if (pin[1].equalsIgnoreCase("slavein")) {
                    pins.add(part.getSpi().getSlaveIn());
                } else if (pin[1].equalsIgnoreCase("slaveselect")) {
                    pins.add(part.getSpi().getSlaveSelect());
                } else {
                    throw new IllegalArgumentException(String.format(
                        "Unknown SPI pin %s in connection %s",
                        pin[1], connection));
                }
            } else {
                throw new IllegalArgumentException(String.format(
                    "Need to specify SPI pin for %s i.e. %s:clock", pin[0], pin[0]));
            }
        }
        return pins;
    }

    private static Part createVia(Properties properties, String name, Map<String, Part> parts) {
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

        return Part.create(via, via, via, via, via);
    }
}

