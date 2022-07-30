import io.github.gaming32.python4j.objects.PyNoneType;
import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.runtime.PyFrame;
import io.github.gaming32.python4j.runtime.PyModule;
import io.github.gaming32.python4j.runtime.PyRuntime;
import io.github.gaming32.python4j.runtime.annotation.PyClassInfo;
import io.github.gaming32.python4j.runtime.annotation.PyMethodInfo;
import io.github.gaming32.python4j.runtime.invoke.CondyBootstraps;
import java.util.LinkedHashMap;
import java.util.Map;

@PyClassInfo(
   codeObj = "ã\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0003\u0000\u0000\u0000\u0000\u0000\u0000\u0000s\u001c\u0000\u0000\u0000\u0097\u0000\u0002\u0000e\u0000d\u0000¦\u0001\u0000\u0000«\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0000d\u0001S\u0000)\u0002uW\u0000\u0000\u0000https://getemoji.com/: ð\u009f\u0098\u008b Get Emoji â\u0080\u0094 All Emojis to â\u009c\u0082ï¸\u008f Copy and ð\u009f\u0093\u008b Paste ð\u009f\u0091\u008cN)\u0001z\u0005print)\u0000ó\u0000\u0000\u0000\u0000z#.\\src\\test\\resources\\simple_test.pyú\b<module>r\u0002\u0000\u0000\u0000\u0001\u0000\u0000\u0000s\u0017\u0000\u0000\u0000ø\u0080\u0005\u0080\u0005Ð\u0006_Ñ\u0000`Ô\u0000`Ð\u0000`Ð\u0000`Ð\u0000`r\u0001\u0000\u0000\u0000"
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
      PyRuntime.call(null, PyRuntime.loadGlobal(this.$globals, "print"), (PyObject)CondyBootstraps.constant<"$const$0",0,0>());
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
