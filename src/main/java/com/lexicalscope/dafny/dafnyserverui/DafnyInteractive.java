package com.lexicalscope.dafny.dafnyserverui;

import static java.lang.Integer.parseInt;

import java.awt.HeadlessException;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import com.lexicalscope.dafny.dafnyservergui.gui.DafnyServerFrame;
import com.lexicalscope.dafny.dafnyservergui.gui.VerificationModel;
import com.lexicalscope.jewel.cli.CliFactory;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

public class DafnyInteractive
{
    private final Arguments arguments;
    private DafnyServerFrame frame;

    public DafnyInteractive(final Arguments arguments) {
        this.arguments = arguments;
    }

    public static void main(final String[] args) throws IOException, InterruptedException, HeadlessException, InvocationTargetException
    {
        final Arguments arguments = CliFactory.parseArguments(Arguments.class, args);
        new DafnyInteractive(arguments).start();
    }

    public void start() throws IOException, HeadlessException, InvocationTargetException, InterruptedException
    {
        URL.setURLStreamHandlerFactory(protocol ->
        {
            if("fileloc".equals(protocol)) {
                return new URLStreamHandler() {
                    @Override
                    protected URLConnection openConnection(final URL url) throws IOException {
                        return new URLConnection(url) {
                            @Override
                            public void connect() throws IOException {
                                // no way to connect to this
                            }
                        };
                    }};
            } else {
                return null;
            }
          });

        final ProcessBuilder pb = new ProcessBuilder(arguments.server());
        pb.redirectErrorStream(true);
        final ExecutorService threadPool = Executors.newCachedThreadPool();
        final Process process = pb.start();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override public void run() {
                try
                {
                    threadPool.shutdownNow();
                }
                finally
                {
                    process.destroy();
                }
            }
        });

        final LinkedBlockingQueue<String> serverOutputBuffer = new LinkedBlockingQueue<String>();

        final VerificationModel verificationModel = new VerificationModel();
        final DafnyServer server = new DafnyServer(process.getInputStream(), process.getOutputStream());
        //server.addOutputListener(logToConsole());
        server.addOutputListener(logToQueue(serverOutputBuffer));

        final BufferedServerOutputParser parser = BufferedServerOutputParser.createServerOutputParser(serverOutputBuffer, arguments.file());
        parser.addEventListener(new ServerEventBroadcaster("EdtForward", edtForward(verificationModel)));

        frame = DafnyServerFrame.show(server, arguments, verificationModel);
        frame.addErrorListener(this::jumpToError);

        threadPool.submit(parser);
        threadPool.submit(server);
    }

    private void jumpToError(final String file, final String line, final String column) {
        if(arguments.isJump()) {
            final List<String> jump = arguments.jump();
            final List<String> jumpArgs = new ArrayList<>();
            for (final String string : jump) {
                jumpArgs.add(String.format(string, file, parseInt(line), parseInt(column)));
            }

            System.out.println("running " + jumpArgs);
            final ProcessBuilder pb = new ProcessBuilder(jumpArgs);
            pb.redirectError(Redirect.INHERIT);
            pb.redirectOutput(Redirect.INHERIT);
            pb.redirectInput(Redirect.INHERIT);
            try {
                final Process process = pb.start();
                process.waitFor(10, TimeUnit.MILLISECONDS);
            } catch (final IOException | InterruptedException e) {
                e.printStackTrace();
            }
            if(arguments.isPop()) {
                stealFocus(arguments.pop());
            }
        }
    }

    private static void stealFocus(final String name) {
        //HWND hwnd = User32.INSTANCE.FindWindow("vim", null); // window class name
        //final HWND hwnd = User32.INSTANCE.FindWindow(null, "vim74"); // window title
        final HWND hwnd = User32.INSTANCE.FindWindow(null, name); // window title
        if (hwnd == null) {
            System.out.println("can't find window to pop");
        } else {
            User32.INSTANCE.ShowWindow(hwnd, 5); // SW_SHOW
            User32.INSTANCE.SetForegroundWindow(hwnd); // bring to front
        }
    }

    private static Consumer<Consumer<ServerEventListener>> edtForward(final ServerEventListener verificationModel)
    {
        return eventToPropogate -> SwingUtilities.invokeLater(() -> eventToPropogate.accept(verificationModel));
    }

    private static ServerOutputListener logToQueue(final BlockingQueue<String> queue) {
        return line -> queue.add(line);
    }

    static ServerOutputListener logToConsole() {
        return message -> {System.out.println(message);};
    }
}

