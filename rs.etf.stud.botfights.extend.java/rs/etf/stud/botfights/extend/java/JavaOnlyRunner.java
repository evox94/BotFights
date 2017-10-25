package rs.etf.stud.botfights.extend.java;

import rs.etf.stud.botfights.core.LoggerUtil;
import rs.etf.stud.botfights.extend.TCPProcRunner;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JavaOnlyRunner extends TCPProcRunner {
    Process process = null;

    public JavaOnlyRunner(Class<? extends JavaGameStateDeserializer> deserializerClass) {
        super(new JavaOnlyTCPSimpleProtocol(deserializerClass));
    }

    @Override
    protected void startProcess(int port) {
        System.out.println(System.getProperty("module-path"));
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        String modulePath = arguments.stream().filter(s -> s.contains("--module-path")).findFirst().orElse(null);
        System.out.println(modulePath);
        File binDir = new File(System.getProperty("java.home"));
        binDir = new File(binDir, "bin");
        File executable = new File(binDir, "java.exe");
        if (!executable.exists()) {
            executable = new File(binDir, "java");
            if (!executable.exists()) {
                LoggerUtil.getLogger().log("JavaProcRunner: Can't locate java executable");
                throw new RuntimeException("Missing java");
            }
        }
        if (executable.exists()) {
            LoggerUtil.getLogger().log("JavaProcRunner: Found java executable at: " + executable.getAbsolutePath());
        }
        List<String> commands = new ArrayList<>();
        commands.add(executable.getAbsolutePath());
        if (modulePath != null) {
            commands.add(modulePath);
        }
        commands.add("-m");
        commands.add("rs.etf.stud.botfights.javaprocrunner/rs.etf.stud.botfights.javaprocrunner.JavaProcRunner");
        commands.add(port+"");
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
        LoggerUtil.getLogger().log("JavaProcRunner: Starting new process with: "+processBuilder.command().stream().reduce((s1, s2) -> s1+" "+s2).orElse("null"));
        try {
            process = processBuilder.start();
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
            errorGobbler.start();
            LoggerUtil.getLogger().log("JavaProcRunner: Process started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void stopProcess() {
        if (process != null) {
            process.destroy();
            try {
                process.waitFor(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            process.destroyForcibly();
            if(process.isAlive()){
                LoggerUtil.getLogger().log("PROCESS IS STILL ALIVE!");
            }
        }
        process = null;
    }

    class StreamGobbler extends Thread {
        InputStream is;

        // reads everything from is until empty.
        StreamGobbler(InputStream is) {
            this.is = is;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line=null;
                while ( (line = br.readLine()) != null)
                    LoggerUtil.getLogger().log(line);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
