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

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.NativeScope;
import blas.*;
import static blas.RuntimeHelper.*;
import static blas.cblas_h.*;
import static jdk.incubator.foreign.CSupport.*;

public class TestBlas {
    private static void setDouble(MemoryAddress addr, int element, double value) {
        MemoryAccess.setDouble(addr, element*C_DOUBLE.byteSize(), value);
    }

    private static double getDouble(MemoryAddress addr, int element) {
        return MemoryAccess.getDouble(addr, element*C_DOUBLE.byteSize());
    }

    public static void main(String[] args) {
        int Layout;
        int transa;

        double alpha, beta;
        int m, n, lda, incx, incy, i;
 
        Layout = CblasColMajor();
        transa = CblasNoTrans();

        m = 4; /* Size of Column ( the number of rows ) */
        n = 4; /* Size of Row ( the number of columns ) */
        lda = 4; /* Leading dimension of 5 * 4 matrix is 5 */
        incx = 1;
        incy = 1;
        alpha = 1;
        beta = 0;
 
        try (var scope = NativeScope.unboundedScope()) {
            var a = scope.allocateArray(C_DOUBLE, m*n);
            var x = scope.allocateArray(C_DOUBLE, n);
            var y = scope.allocateArray(C_DOUBLE, n);
        
            /* The elements of the first column */
            setDouble(a, 0, 1.0);
            setDouble(a, 1, 2.0);
            setDouble(a, 2, 3.0);
            setDouble(a, 3, 4.0);
            /* The elements of the second column */
            setDouble(a, m, 1.0);
            setDouble(a, m + 1, 1.0);
            setDouble(a, m + 2, 1.0);
            setDouble(a, m + 3, 1.0);
            /* The elements of the third column */
            setDouble(a, m*2, 3.0);
            setDouble(a, m*2 + 1, 4.0);
            setDouble(a, m*2 + 2, 5.0);
            setDouble(a, m*2 + 3, 6.0);
            /* The elements of the fourth column */
            setDouble(a, m*3, 5.0);
            setDouble(a, m*3 + 1, 6.0);
            setDouble(a, m*3 + 2, 7.0);
            setDouble(a, m*3 + 3, 8.0);
            /* The elemetns of x and y */
            setDouble(x, 0, 1.0);
            setDouble(x, 1, 2.0);
            setDouble(x, 2, 1.0);
            setDouble(x, 3, 1.0);
            setDouble(y, 0, 0.0);
            setDouble(y, 1, 0.0);
            setDouble(y, 2, 0.0);
            setDouble(y, 3, 0.0);

            cblas_dgemv(Layout, transa, m, n, alpha, a, lda, x, incx, beta, y, incy);
            /* Print y */
            for (i = 0; i < n; i++) {
                System.out.print(String.format(" y%d = %f\n", i, getDouble(y, i)));
            }
        }
    }
}
