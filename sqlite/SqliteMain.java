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

import org.sqlite.Cpointer;
import org.sqlite.Cstring;
import org.sqlite.RuntimeHelper.CScope;
import static jdk.incubator.foreign.MemoryAddress.NULL;
import static org.sqlite.sqlite3_h.*;

public class SqliteMain {
   public static void main(String[] args) throws Exception {
        try (var scope = new CScope()) {
            // char** errMsgPtrPtr;
            var errMsgPtrPtr = Cpointer.allocate(NULL, scope);

            // sqlite3** dbPtrPtr;
            var dbPtrPtr = Cpointer.allocate(NULL, scope);

            int rc = sqlite3_open(Cstring.toCString("employee.db",scope), dbPtrPtr);
            if (rc != 0) {
                System.err.println("sqlite3_open failed: " + rc);
                return;
            } else {
                System.out.println("employee db opened");
            }

            // sqlite3* dbPtr;
            var dbPtr = Cpointer.get(dbPtrPtr);

            // create a new table
            var sql = Cstring.toCString(
                "CREATE TABLE EMPLOYEE ("  +
                "  ID INT PRIMARY KEY NOT NULL," +
                "  NAME TEXT NOT NULL,"    +
                "  SALARY REAL NOT NULL )", scope);

            rc = sqlite3_exec(dbPtr, sql, NULL, NULL, errMsgPtrPtr);

            if (rc != 0) {
                System.err.println("sqlite3_exec failed: " + rc);
                System.err.println("SQL error: " + Cstring.toJavaString(Cpointer.get(errMsgPtrPtr)));
                sqlite3_free(Cpointer.get(errMsgPtrPtr));
            } else {
                System.out.println("employee table created");
            }

            // insert two rows
            sql = Cstring.toCString(
                "INSERT INTO EMPLOYEE (ID,NAME,SALARY) " +
                    "VALUES (134, 'Xyz', 200000.0); " +
                "INSERT INTO EMPLOYEE (ID,NAME,SALARY) " +
                    "VALUES (333, 'Abc', 100000.0);", scope
            );
            rc = sqlite3_exec(dbPtr, sql, NULL, NULL, errMsgPtrPtr);

            if (rc != 0) {
                System.err.println("sqlite3_exec failed: " + rc);
                System.err.println("SQL error: " + Cstring.toJavaString(Cpointer.get(errMsgPtrPtr)));
                sqlite3_free(Cpointer.get(errMsgPtrPtr));
            } else {
                System.out.println("rows inserted");
            }

            int[] rowNum = new int[1];
            // callback to print rows from SELECT query
            var callback = sqlite3_exec$callback.allocate((a, argc, argv, columnNames) -> {
                System.out.println("Row num: " + rowNum[0]++);
                System.out.println("numColumns = " + argc);
                argv = Cpointer.asArray(argv, argc);
                columnNames = Cpointer.asArray(columnNames, argc);
                for (int i = 0; i < argc; i++) {
                     String name = Cstring.toJavaString(Cpointer.get(columnNames, i));
                     String value = Cstring.toJavaString(Cpointer.get(argv, i));
                     System.out.printf("%s = %s\n", name, value);
                }
                return 0;
            });
            scope.register(callback);

            // select query
            sql = Cstring.toCString("SELECT * FROM EMPLOYEE", scope);
            rc = sqlite3_exec(dbPtr, sql, callback.baseAddress(), NULL, errMsgPtrPtr);

            if (rc != 0) {
                System.err.println("sqlite3_exec failed: " + rc);
                System.err.println("SQL error: " + Cstring.toJavaString(Cpointer.get(errMsgPtrPtr)));
                sqlite3_free(Cpointer.get(errMsgPtrPtr));
            } else {
                System.out.println("done");
            }

            sqlite3_close(dbPtr);
        }
    }
}
