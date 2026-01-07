
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author 胡鹏
 */
public class Demo {


    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("org.apache.skywalking.apm.agent.core.context.ContextCarrier");
        Object obj = clazz.getConstructor().newInstance();

        Method setTraceId = clazz.getDeclaredMethod("setTraceId", String.class);
        setTraceId.setAccessible(true);
        setTraceId.invoke(obj, "a5fb2ee78f604340b0ae7812ca82f49f.129.17676790495620001");

        Field traceSegmentId = clazz.getDeclaredField("traceSegmentId");
        traceSegmentId.setAccessible(true);
        traceSegmentId.set(obj, "100");

        Field spanId = clazz.getDeclaredField("spanId");
        spanId.setAccessible(true);
        spanId.set(obj, 100);
        Field parentService = clazz.getDeclaredField("parentService");
        parentService.setAccessible(true);
        parentService.set(obj, "100");
        Field parentServiceInstance = clazz.getDeclaredField("parentServiceInstance");
        parentServiceInstance.setAccessible(true);
        parentServiceInstance.set(obj, "100");
        Field parentEndpoint = clazz.getDeclaredField("parentEndpoint");
        parentEndpoint.setAccessible(true);
        parentEndpoint.set(obj, "100");
        Field addressUsedAtClient = clazz.getDeclaredField("addressUsedAtClient");
        addressUsedAtClient.setAccessible(true);
        addressUsedAtClient.set(obj, "100");


        Class<?> contextManagerClass = Class.forName("org.apache.skywalking.apm.agent.core.context.ContextManager");
        Method createEntrySpanMethod = contextManagerClass.getDeclaredMethod("createEntrySpan", String.class, clazz);
        createEntrySpanMethod.setAccessible(true);
        createEntrySpanMethod.invoke(null, "demoJobHandler", obj);
    }
}
