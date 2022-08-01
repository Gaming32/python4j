import io.github.gaming32.python4j.objects.PyNoneType;
import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.runtime.PyFrame;
import io.github.gaming32.python4j.runtime.PyModule;
import io.github.gaming32.python4j.runtime.PyRuntime;
import io.github.gaming32.python4j.runtime.annotation.PyClassInfo;
import io.github.gaming32.python4j.runtime.annotation.PyMethodInfo;
import io.github.gaming32.python4j.runtime.invoke.CondyBootstraps;
import io.github.gaming32.python4j.runtime.modules.PyBuiltins;
import java.util.LinkedHashMap;
import java.util.Map;

@PyClassInfo(
   codeObj = "ã\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0005\u0000\u0000\u0000\u0000\u0000\u0000\u0000s4\u0000\u0000\u0000\u0097\u0000d\u0000Z\u0000d\u0001Z\u0001d\u0002Z\u0002\u0002\u0000e\u0003e\u0000\u009b\u0000e\u0001\u009b\u0000e\u0002\u009b\u0000\u009d\u0003¦\u0001\u0000\u0000«\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0000d\u0003S\u0000)\u0004z\u0006Hello z\u0006world!u\u0006\u0000\u0000\u0000 ð\u009f\u0098\u008b!N)\u0004z\u0001az\u0001bz\u0001cz\u0005print)\u0000ó\u0000\u0000\u0000\u0000z#.\\src\\test\\resources\\simple_test.pyú\b<module>r\u0002\u0000\u0000\u0000\u0001\u0000\u0000\u0000s1\u0000\u0000\u0000ø\u0080H\u0080\u0001Ø\u0004\f\u0080\u0001Ø\u0004\f\u0080\u0001Ø\u0000\u0005\u0080\u0005\u0088\u0011\u0080l\u0088A\u0080l\u0088q\u0080l\u0080lÑ\u0000\u0013Ô\u0000\u0013Ð\u0000\u0013Ð\u0000\u0013Ð\u0000\u0013r\u0001\u0000\u0000\u0000"
)
public final class simple_test implements PyModule {
   private Map<String, PyObject> $globals = new LinkedHashMap();

   public static void main(String[] var0) {
      new simple_test().init();
   }

   public String getName() {
      return "simple_test";
   }

   public String[] dir() {
      return PyRuntime.moduleDir(this.$globals);
   }

   public PyObject getattr(String var1) {
      return (PyObject)this.$globals.get(var1);
   }

   public boolean setattr(String var1, PyObject var2) {
      this.$globals.set(var1, var2);
      return true;
   }

   public String[] all() {
      return PyRuntime.moduleAll(this.$globals);
   }

   @PyMethodInfo(
      codeRefId = 0
   )
   private PyObject _module_0(PyObject[] var1) {
      PyFrame.push(simple_test.class, 0, var1);
      PyRuntime.storeGlobal((PyObject)CondyBootstraps.constant<"$const$0",0,0>(), this.$globals, "a");
      PyRuntime.storeGlobal((PyObject)CondyBootstraps.constant<"$const$1",0,1>(), this.$globals, "b");
      PyRuntime.storeGlobal((PyObject)CondyBootstraps.constant<"$const$2",0,2>(), this.$globals, "c");
      PyRuntime.call(
         null,
         PyRuntime.loadGlobal(this.$globals, "print"),
         PyRuntime.buildString(
            PyBuiltins.format(PyRuntime.loadGlobal(this.$globals, "a"), null),
            PyBuiltins.format(PyRuntime.loadGlobal(this.$globals, "b"), null),
            PyBuiltins.format(PyRuntime.loadGlobal(this.$globals, "c"), null)
         )
      );
      PyNoneType var10000 = PyNoneType.PyNone;
      PyFrame.pop();
      return var10000;
   }

   public void init() {
      if (this._module_0(new PyObject[0]) != PyNoneType.PyNone) {
         throw new AssertionError("Top-level module code did not return None.");
      }
   }
}
