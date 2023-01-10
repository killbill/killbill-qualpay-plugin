/*
 * Copyright 2014-2019 Groupon, Inc
 * Copyright 2014-2019 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.qualpay;

import java.io.IOException;
import java.sql.SQLException;

import org.killbill.billing.platform.test.PlatformDBTestingHelper;
import org.killbill.billing.plugin.TestUtils;
import org.killbill.billing.plugin.qualpay.dao.QualpayDao;
import org.killbill.commons.embeddeddb.EmbeddedDB;
import org.killbill.commons.embeddeddb.EmbeddedDB.DBEngine;

public class EmbeddedDbHelper {

    private static final EmbeddedDbHelper INSTANCE = new EmbeddedDbHelper();
    private EmbeddedDB embeddedDB;
    
    private static final String DDL_FILE_NAME = "ddl.sql";

    public static EmbeddedDbHelper instance() {
        return INSTANCE;
    }

    public void startDb() throws Exception {

        embeddedDB = PlatformDBTestingHelper.get().getInstance();
        
        // Needed, otherwise get Caused by: java.sql.SQLException: No suitable driver found for jdbc:mysql:<connection-url>
        if (embeddedDB.getDBEngine().equals(DBEngine.MYSQL)) {
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        
        embeddedDB.initialize();
        embeddedDB.start();

        final String databaseSpecificDDL = "ddl-" + embeddedDB.getDBEngine().name().toLowerCase() + ".sql";
        try {
            embeddedDB.executeScript(TestUtils.toString(databaseSpecificDDL));
        } catch (final IllegalArgumentException e) {
            // Ignore, no engine specific DDL
        }

        final String ddl = TestUtils.toString(DDL_FILE_NAME);
        embeddedDB.executeScript(ddl);
        embeddedDB.refreshTableNames();
    }

    public QualpayDao getQualpayDao() throws IOException, SQLException {
        return new QualpayDao(embeddedDB.getDataSource());
    }

    public void resetDB() throws Exception {
        embeddedDB.cleanupAllTables();
    }

    public void stopDB() throws Exception {
        embeddedDB.stop();
    }
}