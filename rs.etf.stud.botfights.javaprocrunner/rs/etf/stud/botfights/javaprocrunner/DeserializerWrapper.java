package rs.etf.stud.botfights.javaprocrunner;

import java.lang.reflect.Method;

public class DeserializerWrapper {
    private static String methodName = "toParams";

    private Object deserializerInstance;
    private Method method;

    private DeserializerWrapper(Object deserializerInstance, Method method) {
        this.deserializerInstance = deserializerInstance;
        this.method = method;
    }

    public Object[] deserialize(String gameState) throws Exception {
        return (Object[]) method.invoke(deserializerInstance, gameState);
    }

    public static DeserializerWrapper fromClassName(String className) throws Exception {
        try {
            Class<?> deserializerClass = null;
            deserializerClass = Class.forName(className);
            Object deserializerInstance = deserializerClass.getDeclaredConstructor().newInstance();
            Method deserializerMethod = deserializerClass.getDeclaredMethod("toParams", String.class);
            return new DeserializerWrapper(deserializerInstance, deserializerMethod);
        } catch (Exception e) {
            throw new Exception("Exception initializing 'GameState' deserializer", e);
        }
    }
}
