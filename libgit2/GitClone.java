
import static com.github.git2_h.*;
import static jdk.incubator.foreign.CSupport.*;
import static jdk.incubator.foreign.MemoryAddress.NULL;
import static jdk.incubator.foreign.NativeAllocationScope.*;
import static com.github.Cstring.*;

public class GitMain {
    public static void main(String[] args) {
          if (args.length != 2) {
              System.err.println("java GitClone <url> <path>");
              System.exit(1);
          }
          git_libgit2_init();
          try (var scope = unboundedScope()) {
              var repo = scope.allocate(C_POINTER, NULL);
              var url = toCString(args[0], scope);
              var path = toCString(args[1], scope);
              System.out.println(git_clone(repo, url, path, NULL));
          }          
          git_libgit2_shutdown();
    }
}
