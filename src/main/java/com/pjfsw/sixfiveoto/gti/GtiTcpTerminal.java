package com.pjfsw.sixfiveoto.gti;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import com.pjfsw.sixfiveoto.addressables.Clockable;

public class GtiTcpTerminal implements Clockable {
    private final ExecutorService executorService;
    private final Supplier<Integer> input;
    private final Function<Integer, Boolean> output;
    private final AtomicReference<GtiTerminal> terminalReference = new AtomicReference<>(null);

    GtiTcpTerminal(ExecutorService executorService, Supplier<Integer> input, Function<Integer,Boolean> output) {
        this.executorService = executorService;
        this.input = input;
        this.output = output;
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        executorService.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(4000)) {
                System.out.println("DERPES");
                while (terminalReference.get() == null) {
                    System.out.println("Waiting for connection");
                    try (Socket socket = serverSocket.accept();
                        InputStream is = socket.getInputStream();
                        OutputStream os = socket.getOutputStream()) {
                        GtiTerminal terminal = new GtiTerminal(input, output, is, os);
                        terminalReference.set(terminal);
                        while (terminalReference.get() != null) {
                            sleep(1000);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sleep(1000);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void next(final int cycles) {
        GtiTerminal terminal = this.terminalReference.get();
        try {
            if (terminal != null) {
                terminal.poll();
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.terminalReference.set(null);
        }
    }
}
