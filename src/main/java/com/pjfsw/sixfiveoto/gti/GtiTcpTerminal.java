package com.pjfsw.sixfiveoto.gti;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.pjfsw.sixfiveoto.addressables.Clockable;

public class GtiTcpTerminal implements Clockable {
    private static final int CAPACITY = 16384;
    private final ExecutorService executorService;
    private final Supplier<Integer> input;
    private final Function<Integer, Boolean> output;
    private final Consumer<Boolean> connectedConsumer;
    private final Queue<Integer> toTerminal;
    private final Queue<Integer> fromTerminal;

    public GtiTcpTerminal(
        ExecutorService executorService,
        Supplier<Integer> input,
        Function<Integer,Boolean> output,
        Consumer<Boolean> connectedConsumer
    ) {
        this.executorService = executorService;
        this.input = input;
        this.output = output;
        this.connectedConsumer = connectedConsumer;
        toTerminal = new ArrayBlockingQueue<>(CAPACITY);
        fromTerminal = new ArrayBlockingQueue<>(CAPACITY);
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
                System.out.println("Waiting for connection");
                try (Socket socket = serverSocket.accept();
                    InputStream is = socket.getInputStream();
                    OutputStream os = socket.getOutputStream()) {
                    connectedConsumer.accept(true);
                    GtiTerminal terminal = new GtiTerminal(
                        toTerminal::poll,
                        fromTerminal::offer,
                        is, os);
                    while (!terminal.isClosed()) {
                        for (int i = 0; i < 1000; i++) {
                            terminal.poll();
                        }
                        sleep(16);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    sleep(1000);
                }
                connectedConsumer.accept(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void next(final int cycles) {
        if (toTerminal.size() < CAPACITY) {
            int v = input.get();
            if (v != -1) {
                toTerminal.offer(v);
            }
        }
        Integer v = fromTerminal.poll();
        if (v != null) {
            output.apply(v);
        }
    }

}
