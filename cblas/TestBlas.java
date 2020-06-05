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

import blas.*;
import static blas.RuntimeHelper.*;
import static blas.cblas_h.*;

public class TestBlas {
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
 
        try (var scope = new CScope()) {
            var a = Cdouble.allocateArray(m*n, scope);
            var x = Cdouble.allocateArray(n, scope);
            var y = Cdouble.allocateArray(n, scope);
        
            /* The elements of the first column */
            Cdouble.set(a, 0, 1.0);
            Cdouble.set(a, 1, 2.0);
            Cdouble.set(a, 2, 3.0);
            Cdouble.set(a, 3, 4.0);
            /* The elements of the second column */
            Cdouble.set(a, m, 1.0);
            Cdouble.set(a, m + 1, 1.0);
            Cdouble.set(a, m + 2, 1.0);
            Cdouble.set(a, m + 3, 1.0);
            /* The elements of the third column */
            Cdouble.set(a, m*2, 3.0);
            Cdouble.set(a, m*2 + 1, 4.0);
            Cdouble.set(a, m*2 + 2, 5.0);
            Cdouble.set(a, m*2 + 3, 6.0);
            /* The elements of the fourth column */
            Cdouble.set(a, m*3, 5.0);
            Cdouble.set(a, m*3 + 1, 6.0);
            Cdouble.set(a, m*3 + 2, 7.0);
            Cdouble.set(a, m*3 + 3, 8.0);
            /* The elemetns of x and y */
            Cdouble.set(x, 0, 1.0);
            Cdouble.set(x, 1, 2.0);
            Cdouble.set(x, 2, 1.0);
            Cdouble.set(x, 3, 1.0);
            Cdouble.set(y, 0, 0.0);
            Cdouble.set(y, 1, 0.0);
            Cdouble.set(y, 2, 0.0);
            Cdouble.set(y, 3, 0.0);

            cblas_dgemv(Layout, transa, m, n, alpha, a, lda, x, incx, beta, y, incy);
            /* Print y */
            for (i = 0; i < n; i++) {
                System.out.print(String.format(" y%d = %f\n", i, Cdouble.get(y, (long)i)));
            }
        }
    }
}
