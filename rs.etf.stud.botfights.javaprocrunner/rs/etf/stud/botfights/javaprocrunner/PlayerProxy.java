package rs.etf.stud.botfights.javaprocrunner;

import javax.tools.JavaCompiler;
import javax.tools.Tool;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerProxy {
    class PlayerThread extends Thread {
        @Override
        public void run() {
            try {
                while (!interrupted()) {
                    playerMethod.invoke(null, paramsQueue.take());
                }
            } catch (InterruptedException ex) {
                //nothing
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        String getPlayerId() {
            return id;
        }
    }

    private String id;
    private String algPathName;
    private BlockingQueue<Object[]> paramsQueue;
    private AtomicInteger callCount = new AtomicInteger(0);

    private PlayerThread playerThread;

    private Method playerMethod;
    public PlayerProxy(String id, String algPathName) {
        this.id = id;
        this.algPathName = algPathName;
        paramsQueue = new ArrayBlockingQueue<Object[]>(20);
        playerThread = new PlayerThread();
    }
    public String getId() {
        return id;
    }

    public static String getCurrentPlayerId() {
        return ((PlayerThread) Thread.currentThread()).getPlayerId();
    }

    public void start() {
        playerThread.start();
    }

    public void stop() {
        playerThread.interrupt();
    }

    public void queryCall(Object[] params) {
        try {
            paramsQueue.put(params);
            callCount.incrementAndGet();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void loadAlgorithm(Tool javac) throws Exception {
        Path alg = Paths.get(algPathName);
        if (!Files.exists(alg)) throw new FileNotFoundException("File:" + alg.toAbsolutePath().toString());

        Path outputDirectory = createOutputDirectory();
        try {
            final List<String> classNames = compile(javac, outputDirectory, alg);
            if(classNames.isEmpty()){
                throw new Exception("No .class file found!");
            }
            playerMethod = load(outputDirectory, classNames);
        } finally {
            deleteDirectory(outputDirectory);
        }
    }

    private void deleteDirectory(Path outputDirectory) throws Exception {
        try (Stream<Path> s = Files.walk(outputDirectory, FileVisitOption.FOLLOW_LINKS);) {
            s.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    private Path createOutputDirectory() throws IOException {
        return Files.createTempDirectory(getClass().getCanonicalName());
    }

    private Method load(Path binDir, List<String> classNames) throws Exception {
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{binDir.toUri().toURL()});
        for(String className: classNames){
            Class<?> clazz = Class.forName(className, true, classLoader);
            if(!Modifier.isPublic(clazz.getModifiers()))continue;
            return Arrays.stream(clazz.getMethods())
                    .filter(method -> Modifier.isPublic(method.getModifiers()))
                    .filter(method -> Modifier.isStatic(method.getModifiers()))
                    .findFirst()
                    .orElseThrow(() -> new Exception(className + " has no public static methods!"));
        }
        throw new Exception("No public classes!");
    }

    private List<String> compile(Tool compiler, Path outputDir, Path source) throws Exception {
        compiler.run(System.in, ((PlayerInterceptor) System.out).getOrg(), System.err, "-d", outputDir.toAbsolutePath().toString(), source.toAbsolutePath().toString());
        try (Stream<Path> stream = Files.walk(outputDir)) {
            return stream.filter(Files::isRegularFile)
                    .filter(s -> s.toString().contains(".class"))
                    .map(val -> outputDir.relativize(val).toString().replaceAll("[\\\\/]", ".").replaceAll(".class", ""))
                    .collect(Collectors.toList());
        }
    }
}
