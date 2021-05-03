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

    static final FunctionDescriptor JIMAGE_OpenFUNC = FunctionDescriptor.of(
        C_POINTER, C_POINTER, C_POINTER);

    static final MethodHandle JIMAGE_OpenMH = LINKER.downcallHandle(
        LOOKUP.lookup("JIMAGE_Open").get(),
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

    static final MethodHandle JIMAGE_CloseMH = LINKER.downcallHandle(
        LOOKUP.lookup("JIMAGE_Close").get(),
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

    static final MethodHandle JIMAGE_ResourceIteratorMH = LINKER.downcallHandle(
        LOOKUP.lookup("JIMAGE_ResourceIterator").get(),
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
        int apply(MemoryAddress jimage, MemoryAddress module_name, MemoryAddress version,
            MemoryAddress package_name, MemoryAddress name, MemoryAddress extension, MemoryAddress arg);

        static MemoryAddress allocate(JIMAGE_ResourceIteratorVisitor fi, ResourceScope scope) {
            try {
                MethodHandle handle = MH_LOOKUP.findVirtual(fi.getClass(), "apply",
                    MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class,
                        MemoryAddress.class, MemoryAddress.class, MemoryAddress.class,
                        MemoryAddress.class, MemoryAddress.class));
                handle = handle.bindTo(fi);
                return LINKER.upcallStub(handle, JIMAGE_ResourceIteratorVisitorFUNC, scope);
            } catch (Throwable th) {
                throw new AssertionError(th);
            }
        }
    }

    public static void main(String[] args) throws Throwable {
        String javaHome = System.getProperty("java.home");
        try (var scope = ResourceScope.newConfinedScope()) {
            var jintResPtr = MemorySegment.allocateNative(C_INT, scope).address();
            var moduleFilePath = toCString(javaHome + "/lib/modules", scope);
            var jimageFile = JIMAGE_Open(moduleFilePath, jintResPtr);

            var visitor = JIMAGE_ResourceIteratorVisitor.allocate(
                (jimage, module_name, version, package_name, name, extension, arg) -> {
                   System.out.println("module " + toJavaString(module_name));
                   System.out.println("package " + toJavaString(package_name));
                   System.out.println("name " + toJavaString(name));
                   return 1;
                }, scope);
            JIMAGE_ResourceIterator(jimageFile, visitor, NULL);

            JIMAGE_Close(jimageFile);
        }
    }
}
