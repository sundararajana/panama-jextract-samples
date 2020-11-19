/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.lang.invoke.*;
import jdk.incubator.foreign.*;
import java.util.*;
import java.util.stream.*;
import static jdk.incubator.foreign.MemoryAddress.NULL;
import static jdk.incubator.foreign.CLinker.*;

public class JImageFileForeignABI {
    private final static CLinker LINKER = CLinker.getInstance();
    private final static MethodHandles.Lookup MH_LOOKUP = MethodHandles.lookup();
    private final static LibraryLookup LOOKUP = LibraryLookup.ofDefault();

    static final MethodHandle downcallHandle(String name, MethodType mtype, FunctionDescriptor fdesc) {
        return LOOKUP.lookup(name).map(
                addr -> {
                    return LINKER.downcallHandle(addr, mtype, fdesc);
                }).orElse(null);
    }

    static final <Z> MemorySegment upcallStub(Class<Z> fi, Z z, MethodType mtype, FunctionDescriptor fdesc) {
        try {
            MethodHandle handle = MH_LOOKUP.findVirtual(fi, "apply", mtype);
            handle = handle.bindTo(z);
            return LINKER.upcallStub(handle, fdesc);
        } catch (Throwable ex) {
            throw new AssertionError(ex);
        }
    }

    static final FunctionDescriptor JIMAGE_OpenFUNC = FunctionDescriptor.of(
        C_POINTER, C_POINTER, C_POINTER);

    static final MethodHandle JIMAGE_OpenMH = downcallHandle(
        "JIMAGE_Open",
        MethodType.methodType(MemoryAddress.class, MemoryAddress.class, MemoryAddress.class),
        JIMAGE_OpenFUNC
    );

    static MemoryAddress JIMAGE_Open(Addressable name, Addressable error) {
        try {
            return (MemoryAddress)JIMAGE_OpenMH.invokeExact(name.address(), error.address());
        } catch (Throwable ex) {
            throw new AssertionError(ex);
        }
    }

    static final FunctionDescriptor JIMAGE_CloseFUNC = FunctionDescriptor.ofVoid(
        C_POINTER
    );

    static final MethodHandle JIMAGE_CloseMH = downcallHandle(
        "JIMAGE_Close",
        MethodType.methodType(void.class, MemoryAddress.class),
        JIMAGE_CloseFUNC
    );

    static void JIMAGE_Close(Addressable jimage) {
        try {
            JIMAGE_CloseMH.invokeExact(jimage.address());
        } catch (Throwable ex) {
            throw new AssertionError(ex);
        }
    }

    static final FunctionDescriptor JIMAGE_ResourceIteratorFUNC = FunctionDescriptor.ofVoid(
        C_POINTER, C_POINTER, C_POINTER);

    static final MethodHandle JIMAGE_ResourceIteratorMH = downcallHandle(
        "JIMAGE_ResourceIterator",
        MethodType.methodType(void.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class),
        JIMAGE_ResourceIteratorFUNC
    );

    static void JIMAGE_ResourceIterator(Addressable jimage, Addressable visitor, Addressable arg) {
        try {
            JIMAGE_ResourceIteratorMH.invokeExact(jimage.address(), visitor.address(), arg.address());
        } catch (Throwable ex) {
            throw new AssertionError(ex);
        }
    }

    private static final FunctionDescriptor JIMAGE_ResourceIteratorVisitorFUNC = FunctionDescriptor.of(
        C_INT, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER
    );

    static interface JIMAGE_ResourceIteratorVisitor {
        int apply(MemoryAddress x0, MemoryAddress x1, MemoryAddress x2,
            MemoryAddress x3, MemoryAddress x4, MemoryAddress x5, MemoryAddress x6);

        static MemorySegment allocate(JIMAGE_ResourceIteratorVisitor fi) {
            return upcallStub(JIMAGE_ResourceIteratorVisitor.class, fi,
                MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class,
                    MemoryAddress.class, MemoryAddress.class, MemoryAddress.class,
                    MemoryAddress.class, MemoryAddress.class),
                JIMAGE_ResourceIteratorVisitorFUNC);
        }

        static  MemorySegment allocate(JIMAGE_ResourceIteratorVisitor fi, NativeScope scope) {
            return allocate(fi).handoff(scope);
        }
    }

    public static void main(String[] args) throws Throwable {
        String javaHome = System.getProperty("java.home");
        try (var scope = NativeScope.unboundedScope()) {
            var jintResPtr = scope.allocate(C_INT, 0);
            var moduleFilePath = toCString(javaHome + "/lib/modules", scope);
            var jimageFile = JIMAGE_Open(moduleFilePath, jintResPtr);

            var visitor = JIMAGE_ResourceIteratorVisitor.allocate(
                (jimage, module_name, version, package_name, name, extension, arg) -> {
                   System.out.println("module " + toJavaStringRestricted(module_name));
                   System.out.println("package " + toJavaStringRestricted(package_name));
                   System.out.println("name " + toJavaStringRestricted(name));
                   return 1;
                }, scope);
            JIMAGE_ResourceIterator(jimageFile, visitor, NULL);

            JIMAGE_Close(jimageFile);
        }
    }
}
