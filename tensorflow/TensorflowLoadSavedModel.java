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

import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.CSupport.*;
import static jdk.incubator.foreign.MemoryAddress.*;
import static org.tensorflow.c_api_h.*;
import static org.tensorflow.RuntimeHelper.*;

// simple program that loads saved model and prints basic info on operations in it

public class TensorflowLoadSavedModel {
    public static void main(String... args) throws Exception {
        System.out.println("TensorFlow C library version: " + toJavaStringRestricted(TF_Version()));

        if (args.length == 0) {
            System.err.println("java TensorflowLoadSavedModel <saved model dir>");
            System.exit(1);
        }

        try (var scope = NativeScope.unboundedScope()) {
            var graph = TF_NewGraph();
            var status = TF_NewStatus();
            var sessionOpts = TF_NewSessionOptions();

            var savedModelDir = toCString(args[0], scope);
            var tags = scope.allocate(C_POINTER);
            MemoryAccess.setAddressAtOffset(tags, 0, toCString("serve", scope).address());
            var session = TF_LoadSessionFromSavedModel(sessionOpts, NULL, savedModelDir, tags, 1, graph, NULL, status);

            if (TF_GetCode(status) != TF_OK()) {
                System.err.printf("cannot load session from saved model: %s\n",
                    toJavaStringRestricted(TF_Message(status)));
            } else {
                System.err.println("load session from saved model works!");
            }

            // print operations
            var size = scope.allocate(C_LONGLONG);
            var operation = NULL;
            while (!(operation = TF_GraphNextOperation(graph, size.address())).equals(NULL)) {
                System.out.printf("%s : %s\n",
                    toJavaStringRestricted(TF_OperationName(operation)),
                    toJavaStringRestricted(TF_OperationOpType(operation)));
            }

            TF_DeleteGraph(graph);
            TF_DeleteSession(session, status);
            TF_DeleteSessionOptions(sessionOpts);
            TF_DeleteStatus(status);
        }
    }
}
