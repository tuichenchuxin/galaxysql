/*
 * Copyright [2013-2021], Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.polardbx.qatest.ddl.sharding.cdc;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 针对分区表的DDL打标测试
 * <p>
 * created by ziyang.lb
 **/
@Ignore
public class CdcPartitionTableDdlRecordTest extends CdcBaseTest {
    private static final String CREATE_RANGE_TABLE_SQL = "CREATE TABLE `%s` (\n"
        + "     `a` datetime NOT NULL,\n"
        + "     `b` int NOT NULL,\n"
        + "     `c` varchar(20) NOT NULL,\n"
        + "     `d` datetime NOT NULL,\n"
        + "     `e` int NOT NULL,\n"
        + "     `f` bigint NOT NULL,\n"
        + "     `g` bigint NOT NULL,\n"
        + "     `h` bigint NOT NULL,\n"
        + "     `i` bigint NOT NULL\n"
        + ") ENGINE = InnoDB DEFAULT CHARSET = utf8mb4  \n"
        + "PARTITION BY RANGE(YEAR(a))\n"
        + "(PARTITION p0 VALUES LESS THAN (1990) ENGINE = InnoDB,\n"
        + " PARTITION p1 VALUES LESS THAN (2000) ENGINE = InnoDB,\n"
        + " PARTITION p2 VALUES LESS THAN (2010) ENGINE = InnoDB,\n"
        + " PARTITION p3 VALUES LESS THAN (2020) ENGINE = InnoDB)";

    private static final String CREATE_RANGE_COLUMN_TABLE_SQL = "CREATE TABLE `%s` (\n"
        + "     `id` bigint not null,\n"
        + "     `a` datetime NOT NULL,\n"
        + "     `b` datetime NOT NULL,\n"
        + "     `c` varchar(20) NOT NULL,\n"
        + "     `d` datetime NOT NULL,\n"
        + "     `e` int NOT NULL,\n"
        + "     `f` bigint NOT NULL,\n"
        + "     `g` bigint NOT NULL,\n"
        + "     `h` bigint NOT NULL,\n"
        + "     `i` bigint NOT NULL,\n"
        + "     PRIMARY KEY (`id`) \n"
        + ") ENGINE = InnoDB DEFAULT CHARSET = utf8mb4  \n"
        + "PARTITION BY RANGE COLUMNS(a)\n"
        + "(PARTITION p0 VALUES LESS THAN ('1991-01-01') ENGINE = InnoDB,\n"
        + " PARTITION p1 VALUES LESS THAN ('2001-01-01') ENGINE = InnoDB,\n"
        + " PARTITION p2 VALUES LESS THAN ('2011-01-01') ENGINE = InnoDB,\n"
        + " PARTITION p3 VALUES LESS THAN ('2021-01-01') ENGINE = InnoDB,\n"
        + " PARTITION p4 VALUES LESS THAN ('2031-01-01') ENGINE = InnoDB);";

    private static final String CREATE_HASH_TABLE_SQL = "CREATE TABLE `%s` (\n"
        + "     `a` datetime NOT NULL,\n"
        + "     `b` int NOT NULL,\n"
        + "     `c` varchar(20) NOT NULL,\n"
        + "     `d` datetime NOT NULL,\n"
        + "     `e` int NOT NULL,\n"
        + "     `f` bigint NOT NULL,\n"
        + "     `g` bigint NOT NULL,\n"
        + "     `h` bigint NOT NULL,\n"
        + "     `i` bigint NOT NULL\n"
        + ") ENGINE = InnoDB DEFAULT CHARSET = utf8mb4  \n"
        + "PARTITION BY HASH(TO_DAYS(a)) PARTITIONS 8";

    private static final String CREATE_HASH_KEY_TABLE_SQL = "CREATE TABLE `%s` (\n"
        + "     `id` bigint not null,\n"
        + "     `a` datetime NOT NULL,\n"
        + "     `b` datetime NOT NULL,\n"
        + "     `c` varchar(20) NOT NULL,\n"
        + "     `d` datetime NOT NULL,\n"
        + "     `e` int NOT NULL,\n"
        + "     `f` bigint NOT NULL,\n"
        + "     `g` bigint NOT NULL,\n"
        + "     `h` bigint NOT NULL,\n"
        + "     `i` bigint NOT NULL,\n"
        + "     PRIMARY KEY (`id`) \n"
        + ") ENGINE = InnoDB DEFAULT CHARSET = utf8mb4  \n"
        + "PARTITION BY KEY(id) PARTITIONS 8";

    private static final String CREATE_LIST_TABLE_SQL = "CREATE TABLE `%s` (\n"
        + "     `id` bigint not null,\n"
        + "     `a` datetime NOT NULL,\n"
        + "     `b` datetime NOT NULL,\n"
        + "     `c` varchar(20) NOT NULL,\n"
        + "     `d` datetime NOT NULL,\n"
        + "     `e` int NOT NULL,\n"
        + "     `f` bigint NOT NULL,\n"
        + "     `g` bigint NOT NULL,\n"
        + "     `h` bigint NOT NULL,\n"
        + "     `i` bigint NOT NULL,\n"
        + "     PRIMARY KEY (`id`) \n"
        + ") ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 \n"
        + "PARTITION BY LIST(YEAR(a))\n"
        + "(PARTITION p0 VALUES IN (1990,1991,1992) ENGINE = InnoDB,\n"
        + " PARTITION p1 VALUES IN (1800,1801,1802) ENGINE = InnoDB,\n"
        + " PARTITION p2 VALUES IN (2010,2012,2013) ENGINE = InnoDB,\n"
        + " PARTITION p3 VALUES IN (2020,2022) ENGINE = InnoDB,\n"
        + " PARTITION p4 VALUES IN (2050,2052) ENGINE = InnoDB)";

    private static final String CREATE_LIST_COLUMN_TABLE_SQL = "CREATE TABLE `%s` (\n"
        + "     `id` bigint not null,\n"
        + "     `a` datetime NOT NULL,\n"
        + "     `b` datetime NOT NULL,\n"
        + "     `c` varchar(20) NOT NULL,\n"
        + "     `d` datetime NOT NULL,\n"
        + "     `e` int NOT NULL,\n"
        + "     `f` bigint NOT NULL,\n"
        + "     `g` bigint NOT NULL,\n"
        + "     `h` bigint NOT NULL,\n"
        + "     `i` bigint NOT NULL,\n"
        + "     PRIMARY KEY (`id`) \n"
        + ") ENGINE = InnoDB DEFAULT CHARSET = utf8mb4  \n"
        + "PARTITION BY LIST COLUMNS(a)\n"
        + "(PARTITION p0 VALUES IN ('1990-01-01','1991-01-01','1992-01-01') ENGINE = InnoDB,\n"
        + " PARTITION p1 VALUES IN ('1800-01-01','1801-01-01','1802-01-01') ENGINE = InnoDB,\n"
        + " PARTITION p2 VALUES IN ('2010-01-01','2012-01-01','2013-01-01') ENGINE = InnoDB,\n"
        + " PARTITION p3 VALUES IN ('2020-01-01','2022-01-01') ENGINE = InnoDB,\n"
        + " PARTITION p4 VALUES IN ('2050-01-01','2052-01-01') ENGINE = InnoDB)";

    private static final String CREATE_BROADCAST_TABLE_SQL = "CREATE TABLE `%s` (\n"
        + "     `id` bigint not null,\n"
        + "     `a` datetime NOT NULL,\n"
        + "     `b` datetime NOT NULL,\n"
        + "     `c` varchar(20) NOT NULL,\n"
        + "     `d` datetime NOT NULL,\n"
        + "     `e` int NOT NULL,\n"
        + "     `f` bigint NOT NULL,\n"
        + "     `g` bigint NOT NULL,\n"
        + "     `h` bigint NOT NULL,\n"
        + "     `i` bigint NOT NULL,\n"
        + "     PRIMARY KEY (`id`) \n"
        + ") ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 broadcast \n";

    private static final String CREATE_SINGLE_TABLE_SQL = "CREATE TABLE `%s` (\n"
        + "     `id` bigint not null,\n"
        + "     `a` datetime NOT NULL,\n"
        + "     `b` datetime NOT NULL,\n"
        + "     `c` varchar(20) NOT NULL,\n"
        + "     `d` datetime NOT NULL,\n"
        + "     `e` int NOT NULL,\n"
        + "     `f` bigint NOT NULL,\n"
        + "     `g` bigint NOT NULL,\n"
        + "     `h` bigint NOT NULL,\n"
        + "     `i` bigint NOT NULL,\n"
        + "     PRIMARY KEY (`id`) \n"
        + ") single \n";

    @Test
    public void testCdcDdlRecord() throws SQLException, InterruptedException {
        String sql;
        String tokenHints;
        try (Statement stmt = tddlConnection.createStatement()) {
            stmt.executeQuery("select database()");

            tokenHints = buildTokenHints();
            sql = tokenHints + "drop database if exists ddl_test_tg";
            stmt.execute(sql);
            Thread.sleep(2000);
            Assert.assertEquals(sql, getDdlRecordSql(tokenHints));

            tokenHints = buildTokenHints();
            sql = tokenHints + "create database ddl_test_tg mode = 'auto'";
            stmt.execute(sql);
            Assert.assertEquals(sql, getDdlRecordSql(tokenHints));

            sql = "use ddl_test_tg";
            stmt.execute(sql);

            testHotKeyExtract4Hash(stmt);
            testHotKeyExtract4HashKey(stmt);
            testHotKeySplitByGroup(stmt);
            testHotKeySplitByTable(stmt);

            boolean createWithGsi = true;
            doDDl(stmt, "t_range_1", "t_range_2", PartitionType.Range, createWithGsi);
            doDDl(stmt, "t_range_column_1", "t_range_column_2", PartitionType.RangeColumn, createWithGsi);

            doDDl(stmt, "t_hash_1", "t_hash_2", PartitionType.Hash, createWithGsi);
            doDDl(stmt, "t_hash_key_1", "t_hash_key_2", PartitionType.HashKey, createWithGsi);

            doDDl(stmt, "t_list_1", "t_list_2", PartitionType.List, createWithGsi);
            doDDl(stmt, "t_list_column_1", "t_list_column_2", PartitionType.ListColumn, createWithGsi);

            doDDl(stmt, "t_broadcast_1", "t_broadcast_2", PartitionType.Broadcast, createWithGsi);
            doDDl(stmt, "t_single_1", "t_single_2", PartitionType.Single, createWithGsi);

            tokenHints = buildTokenHints();
            sql = tokenHints + "drop database ddl_test_tg";
            stmt.execute(sql);
            stmt.execute("use __cdc__");
            Assert.assertEquals(sql, getDdlRecordSql(tokenHints));
        }
    }

    private void doDDl(Statement stmt, String tableName1, String tableName2,
                       PartitionType partitionType, boolean createWithGsi)
        throws SQLException {
        String sql;
        String tokenHints;

        // Test Step
        tokenHints = buildTokenHints();
        sql = tokenHints + String.format("drop table if exists %s ", tableName1);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));

        // Test Step
        tokenHints = buildTokenHints();
        sql = tokenHints + String.format("drop table if exists %s ", tableName2);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));

        // Test Step
        for (int i = 0; i < 2; i++) {
            if (partitionType == PartitionType.Range) {
                sql = String.format(CREATE_RANGE_TABLE_SQL, i == 0 ? tableName1 : tableName2);
            } else if (partitionType == PartitionType.RangeColumn) {
                sql = String.format(CREATE_RANGE_COLUMN_TABLE_SQL, i == 0 ? tableName1 : tableName2);
            } else if (partitionType == PartitionType.Hash) {
                sql = String.format(CREATE_HASH_TABLE_SQL, i == 0 ? tableName1 : tableName2);
            } else if (partitionType == PartitionType.HashKey) {
                sql = String.format(CREATE_HASH_KEY_TABLE_SQL, i == 0 ? tableName1 : tableName2);
            } else if (partitionType == PartitionType.List) {
                sql = String.format(CREATE_LIST_TABLE_SQL, i == 0 ? tableName1 : tableName2);
            } else if (partitionType == PartitionType.ListColumn) {
                sql = String.format(CREATE_LIST_COLUMN_TABLE_SQL, i == 0 ? tableName1 : tableName2);
            } else if (partitionType == PartitionType.Broadcast) {
                sql = String.format(CREATE_BROADCAST_TABLE_SQL, i == 0 ? tableName1 : tableName2);
            } else if (partitionType == PartitionType.Single) {
                sql = String.format(CREATE_SINGLE_TABLE_SQL, i == 0 ? tableName1 : tableName2);
            } else {
                throw new RuntimeException("not supported ");
            }
            tokenHints = buildTokenHints();
            sql = tokenHints + sql;
            stmt.execute(sql);
            //打标的建表语句和传入的建表语句并不完全一样，此处只演示是否是create语句
            Assert.assertTrue(StringUtils.startsWith(getDdlRecordSql(tokenHints), tokenHints));
        }

        tableName1 = testCommonDdl(stmt, tableName1, partitionType, createWithGsi);
        if (partitionType.isPartitionTable()) {
            testTableGroupDdl(stmt, tableName1, tableName2, partitionType, createWithGsi);
        }

        // Test Step
        tokenHints = buildTokenHints();
        sql = tokenHints + String.format("drop table %s", tableName1);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));

        // Test Step
        tokenHints = buildTokenHints();
        sql = tokenHints + String.format("drop table %s", tableName2);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));
    }

    private void testHotKeySplitByGroup(Statement stmt) throws SQLException {
        List<String> tables = new ArrayList<>();
        tables.add("t_orders_hotkey_test_by_group_1");
        tables.add("t_orders_hotkey_test_by_group_2");
        for (String table : tables) {
            String sql1 = "CREATE TABLE `" + table + "` (\n"
                + "        `id` int(11) NOT NULL AUTO_INCREMENT,\n"
                + "        `seller_id` int(11) DEFAULT NULL,\n"
                + "        PRIMARY KEY (`id`),\n"
                + "        KEY `auto_shard_key_seller_id_id` USING BTREE (`seller_id`, `id`)\n"
                + ") ENGINE = InnoDB AUTO_INCREMENT = 1675420 DEFAULT CHARSET = utf8mb4"
                + " PARTITION BY KEY(`seller_id`,`id`)  PARTITIONS 8";
            String sql2 = "insert into " + table
                + " (seller_id) values(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(88),(88),(88),(88);";
            String sql3 =
                "insert into " + table + " (seller_id) select seller_id from " + table + " where seller_id=88;";
            stmt.executeUpdate(sql1);
            stmt.executeUpdate(sql2);
            stmt.executeUpdate(sql3);
            stmt.executeUpdate(sql3);
            stmt.executeUpdate(sql3);
        }

        String tableGroup = queryTableGroup(tables.get(0));
        String tokenHints = buildTokenHints();
        String sql = tokenHints + " alter tablegroup " + tableGroup + " split into partitions 10 by hot value(88);";
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));
        Assert.assertEquals(2, getDdlRecordSqlCount(tokenHints));
        Assert.assertEquals(getDdlRecordTopology(tokenHints, tables.get(0)), queryTopology(tables.get(0)));
        Assert.assertEquals(getDdlRecordTopology(tokenHints, tables.get(1)), queryTopology(tables.get(1)));
    }

    private void testHotKeySplitByTable(Statement stmt) throws SQLException {
        List<String> tables = new ArrayList<>();
        tables.add("t_orders_hotkey_test_by_table_1");
        for (String table : tables) {
            String sql1 = "CREATE TABLE `" + table + "` (\n"
                + "        `id` int(11) NOT NULL AUTO_INCREMENT,\n"
                + "        `seller_id` int(11) DEFAULT NULL,\n"
                + "        PRIMARY KEY (`id`),\n"
                + "        KEY `auto_shard_key_seller_id_id` USING BTREE (`seller_id`, `id`)\n"
                + ") ENGINE = InnoDB AUTO_INCREMENT = 1675420 DEFAULT CHARSET = utf8mb4"
                + " PARTITION BY KEY(`seller_id`,`id`)  PARTITIONS 8";
            String sql2 = "insert into " + table
                + " (seller_id) values(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(88),(88),(88),(88);";
            String sql3 =
                "insert into " + table + " (seller_id) select seller_id from " + table + " where seller_id=88;";
            stmt.executeUpdate(sql1);
            stmt.executeUpdate(sql2);
            stmt.executeUpdate(sql3);
            stmt.executeUpdate(sql3);
            stmt.executeUpdate(sql3);
        }

        String tokenHints = buildTokenHints();
        String sql = tokenHints + " alter table " + tables.get(0) + " split into partitions 20 by hot value(88);";
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));
        Assert.assertEquals(1, getDdlRecordSqlCount(tokenHints));
        Assert.assertEquals(getDdlRecordTopology(tokenHints, tables.get(0)), queryTopology(tables.get(0)));
    }

    private void testHotKeyExtract4HashKey(Statement stmt) throws SQLException {
        List<String> tables = new ArrayList<>();
        tables.add("t_orders_hotkey_extract_1");
        tables.add("t_orders_hotkey_extract_2");
        for (String table : tables) {
            String sql1 = "CREATE TABLE `" + table + "` (\n"
                + "        `id` int(11) NOT NULL AUTO_INCREMENT,\n"
                + "        `seller_id` int(11) DEFAULT NULL,\n"
                + "        PRIMARY KEY (`id`),\n"
                + "        KEY `auto_shard_key_seller_id_id` USING BTREE (`seller_id`, `id`)\n"
                + ") ENGINE = InnoDB AUTO_INCREMENT = 1675420 DEFAULT CHARSET = utf8mb4"
                + " PARTITION BY KEY(`seller_id`)  PARTITIONS 8";
            String sql2 = "insert into " + table
                + " (seller_id) values(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(88),(88),(88),(88);";
            String sql3 =
                "insert into " + table + " (seller_id) select seller_id from " + table + " where seller_id=88;";
            stmt.executeUpdate(sql1);
            stmt.executeUpdate(sql2);
            stmt.executeUpdate(sql3);
            stmt.executeUpdate(sql3);
            stmt.executeUpdate(sql3);
        }

        String tableGroup = queryTableGroup(tables.get(0));
        String tokenHints = buildTokenHints();
        String sql = tokenHints + " alter tablegroup " + tableGroup + " extract to partition by hot value(88[]);";
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));
        Assert.assertEquals(2, getDdlRecordSqlCount(tokenHints));
        Assert.assertEquals(getDdlRecordTopology(tokenHints, tables.get(0)), queryTopology(tables.get(0)));
        Assert.assertEquals(getDdlRecordTopology(tokenHints, tables.get(1)), queryTopology(tables.get(1)));
    }

    private void testHotKeyExtract4Hash(Statement stmt) throws SQLException {
        List<String> tables = new ArrayList<>();
        tables.add("t_orders_hotkey_extract_3");
        tables.add("t_orders_hotkey_extract_4");
        for (String table : tables) {
            String sql1 = "CREATE TABLE `" + table + "` (\n"
                + "        `id` int(11) NOT NULL AUTO_INCREMENT,\n"
                + "        `seller_id` int(11) DEFAULT NULL,\n"
                + "        PRIMARY KEY (`id`),\n"
                + "        KEY `auto_shard_key_seller_id_id` USING BTREE (`seller_id`, `id`)\n"
                + ") ENGINE = InnoDB AUTO_INCREMENT = 1675420 DEFAULT CHARSET = utf8mb4"
                + " PARTITION BY HASH(`seller_id`)  PARTITIONS 8";
            String sql2 = "insert into " + table
                + " (seller_id) values(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(88),(88),(88),(88);";
            String sql3 =
                "insert into " + table + " (seller_id) select seller_id from " + table + " where seller_id=88;";
            stmt.executeUpdate(sql1);
            stmt.executeUpdate(sql2);
            stmt.executeUpdate(sql3);
            stmt.executeUpdate(sql3);
            stmt.executeUpdate(sql3);
        }

        String tableGroup = queryTableGroup(tables.get(0));
        String tokenHints = buildTokenHints();
        String sql = tokenHints + " alter tablegroup " + tableGroup + " extract to partition by hot value(88[]);";
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));
        Assert.assertEquals(2, getDdlRecordSqlCount(tokenHints));
        Assert.assertEquals(getDdlRecordTopology(tokenHints, tables.get(0)), queryTopology(tables.get(0)));
        Assert.assertEquals(getDdlRecordTopology(tokenHints, tables.get(1)), queryTopology(tables.get(1)));
    }

    private String testCommonDdl(Statement stmt, String tableName1,
                                 PartitionType partitionType, boolean testWithGsi)
        throws SQLException {

        String sql;
        String tokenHints;

        //--------------------------------------------------------------------------------
        //-----------------------------------Test Columns---------------------------------
        //--------------------------------------------------------------------------------
        // Test Step
        tokenHints = buildTokenHints();
        sql =
            tokenHints + String.format("alter table %s add column add1 varchar(20) not null default '111'", tableName1);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));

        // Test Step
        tokenHints = buildTokenHints();
        sql =
            tokenHints + String
                .format("alter table %s add column add2 varchar(20) not null default '222' after b", tableName1);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));

        // Test Step
        tokenHints = buildTokenHints();
        sql =
            tokenHints + String.format("alter table %s add column add3 bigint default 0,drop column add2", tableName1);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));

        // Test Step
        tokenHints = buildTokenHints();
        sql = tokenHints + String.format("alter table %s modify add1 varchar(50) not null default '111'", tableName1);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));

        // Test Step
        tokenHints = buildTokenHints();
        sql = tokenHints + String
            .format("alter table %s change column add1 add111 varchar(50) not null default '111'", tableName1);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));

        // Test Step
        tokenHints = buildTokenHints();
        sql = tokenHints + String.format("alter table %s drop column add111", tableName1);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));

        // Test Step
        tokenHints = buildTokenHints();
        sql = tokenHints + String.format("alter table %s drop column add3", tableName1);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));

        //--------------------------------------------------------------------------------
        //-------------------------------Test Local Indexes-------------------------------
        //--------------------------------------------------------------------------------
        // Test Step
        tokenHints = buildTokenHints();
        sql = tokenHints + String.format("alter table %s add index idx_test(`b`)", tableName1);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));

        // Test Step
        tokenHints = buildTokenHints();
        sql = tokenHints + String.format("alter table %s add unique idx_job(`c`)", tableName1);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));

        // Test Step
        tokenHints = buildTokenHints();
        sql = tokenHints + String.format("create index idx_gmt on %s(`d`)", tableName1);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));

        // Test Step
        // 对于含有聚簇索引的表，引擎不支持一个语句里drop两个index，所以一个语句包含两个drop的sql就不用测试了
        // 否则会报错：optimize error by Do not support multi ALTER statements on table with clustered index
        // tokenHints = buildTokenHints();
        // sql = tokenHints + String.format("alter table %s drop index idx_test", tableName1);
        // stmt.execute(sql);
        // Assert.assertEquals(sql, getDdlRecordSql(tokenHints));

        // Test Step
        tokenHints = buildTokenHints();
        sql = tokenHints + String.format("drop index idx_gmt on %s", tableName1);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));

        //--------------------------------------------------------------------------------
        //--------------------------------------Test Gsi----------------------------------
        //--------------------------------------------------------------------------------
        // single表和broadcast表，不支持gsi，无需测试
        if (partitionType.isPartitionTable() && testWithGsi) {
            // Test Step
            tokenHints = buildTokenHints();
            sql =
                tokenHints + String
                    .format("CREATE GLOBAL INDEX g_i_test ON %s (`e`) PARTITION BY HASH(`e`)", tableName1);
            stmt.execute(sql);
            Assert.assertEquals("", getDdlRecordSql(tokenHints));//GSI类型，不进行打标
            Assert.assertEquals(0, getDdlRecordSqlCount(tokenHints));//GSI类型，不进行打标

            // Test Step
            tokenHints = buildTokenHints();
            sql = tokenHints + String.format("alter table %s add GLOBAL INDEX g_i_test11 ON %s (`f`) COVERING "
                + "(`GMT_CREATED`) PARTITION BY HASH(`f`)", tableName1, tableName1);
            //+ "add column add1 varchar(20) not null default '111'";//gsi不支持混合模式，省事儿了，不用测了
            stmt.execute(sql);
            Assert.assertEquals("", getDdlRecordSql(tokenHints));
            Assert.assertEquals(0, getDdlRecordSqlCount(tokenHints));//GSI类型，不进行打标

            // Test Step
            tokenHints = buildTokenHints();
            sql = tokenHints + String.format("drop index g_i_test on %s", tableName1);
            stmt.execute(sql);
            Assert.assertEquals("", getDdlRecordSql(tokenHints));//GSI类型，不进行打标
            Assert.assertEquals(0, getDdlRecordSqlCount(tokenHints));//GSI类型，不进行打标

            // Test Step
            tokenHints = buildTokenHints();
            sql = tokenHints + String.format("alter table %s drop index g_i_test11", tableName1);
            stmt.execute(sql);
            Assert.assertEquals("", getDdlRecordSql(tokenHints));//GSI类型，不进行打标
            Assert.assertEquals(0, getDdlRecordSqlCount(tokenHints));//GSI类型，不进行打标

            // Test Step
            tokenHints = buildTokenHints();
            sql = tokenHints + String
                .format("alter table %s add clustered index `idx1122`(`e`) "
                    + "PARTITION BY RANGE COLUMNS(e) (\n"
                    + "PARTITION p0001 VALUES LESS THAN (100000000),\n"
                    + "PARTITION p0002 VALUES LESS THAN (200000000),\n"
                    + "PARTITION p0003 VALUES LESS THAN (300000000),\n"
                    + "PARTITION p0004 VALUES LESS THAN (400000000),\n"
                    + "PARTITION p0005 VALUES LESS THAN MAXVALUE\n"
                    + ")", tableName1);
            stmt.execute(sql);
            Assert.assertEquals("", getDdlRecordSql(tokenHints));//GSI类型，不进行打标
            Assert.assertEquals(0, getDdlRecordSqlCount(tokenHints));//GSI类型，不进行打标

            // Test Step
            tokenHints = buildTokenHints();
            sql = tokenHints + String.format("alter table %s drop index idx1122", tableName1);
            stmt.execute(sql);
            Assert.assertEquals("", getDdlRecordSql(tokenHints));//GSI类型，不进行打标
            Assert.assertEquals(0, getDdlRecordSqlCount(tokenHints));//GSI类型，不进行打标

            // Test Step
            tokenHints = buildTokenHints();
            sql = tokenHints + String
                .format("alter table %s add column ccc111 timestamp default current_timestamp", tableName1);
            stmt.execute(sql);
            Assert.assertEquals(sql, getDdlRecordSql(tokenHints));//GSI类型，不进行打标
            Assert.assertEquals(1, getDdlRecordSqlCount(tokenHints));//GSI类型，不进行打标
        }

        //--------------------------------------------------------------------------------
        //------------------------------------Test Truncate ------------------------------
        //--------------------------------------------------------------------------------
        // Test Step
        // 如果是带有GSI的表，会报错: Does not support truncate table with global secondary index，so use t_ddl_test_zzz for test
        tokenHints = buildTokenHints();
        sql = tokenHints + String.format("truncate table %s", tableName1);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));

        //--------------------------------------------------------------------------------
        //------------------------------------Test rename --------------------------------
        //--------------------------------------------------------------------------------
        // Test Step
        // 如果是带有GSI的表，会报错: Does not support modify primary table 't_ddl_test' cause global secondary index exists,so use t_ddl_test_yyy for test

        tokenHints = buildTokenHints();
        String newTableName = tableName1 + "_new";
        sql = tokenHints + String.format("rename table %s to %s", tableName1, newTableName);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));
        return newTableName;
    }

    private void testTableGroupDdl(Statement stmt, String tableName1, String tableName2,
                                   PartitionType partitionType, boolean createWithGsi) throws SQLException {
        String sql;
        String tokenHints;
        String tableGroup = queryTableGroup(tableName2);

        if (partitionType.isSupportAddPartition()) {
            tokenHints = buildTokenHints();
            sql = tokenHints + getAddPartitionSql(tableGroup, partitionType);
            stmt.execute(sql);
            checkTableGroupDdl(sql, tokenHints, tableName1, tableName2);
        }

        List<String> splitSqls = getSplitPartitionSql(tableGroup, tableName1, partitionType);
        for (String sqlItem : splitSqls) {
            tokenHints = buildTokenHints();
            sql = tokenHints + sqlItem;
            stmt.execute(sql);
            checkTableGroupDdl(sql, tokenHints, tableName1, tableName2);
        }

        tokenHints = buildTokenHints();
        sql = tokenHints + getMergePartitionSql(tableGroup, partitionType);
        stmt.execute(sql);
        checkTableGroupDdl(sql, tokenHints, tableName1, tableName2);

        if (partitionType.isSupportMovePartition()) {
            tokenHints = buildTokenHints();
            sql = tokenHints + getMovePartitionSql(tableGroup, tableName1, partitionType);
            stmt.execute(sql);
            checkTableGroupDdl(sql, tokenHints, tableName1, tableName2);
        }

        tokenHints = buildTokenHints();
        sql = tokenHints + getTruncatePartitionSql(tableName1, partitionType);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));
        Assert.assertEquals(1, getDdlRecordSqlCount(tokenHints));

        tokenHints = buildTokenHints();
        sql = tokenHints + getTruncatePartitionSql(tableName2, partitionType);
        stmt.execute(sql);
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));
        Assert.assertEquals(1, getDdlRecordSqlCount(tokenHints));

        if (partitionType.isSupportDropPartition()) {
            tokenHints = buildTokenHints();
            sql = tokenHints + getDropPartitionSql(tableGroup, partitionType);
            stmt.execute(sql);
            checkTableGroupDdl(sql, tokenHints, tableName1, tableName2);
        }
    }

    private String queryTableGroup(String tableName) throws SQLException {
        try (Statement stmt = tddlConnection.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("show tablegroup")) {
                while (rs.next()) {
                    String schemaName = rs.getString("TABLE_SCHEMA");
                    String tables = rs.getString("TABLES");
                    String tableGroup = rs.getString("TABLE_GROUP_NAME");
                    String[] tableNames = StringUtils.split(tables, ",");
                    List<String> tableList = Lists.newArrayList(tableNames);

                    if ("ddl_test_tg".equals(schemaName) && tableList.contains(tableName)) {
                        return tableGroup;
                    }
                }
            }
        }

        throw new RuntimeException("can`t find table group for table " + tableName);
    }

    private void checkTableGroupDdl(String sql, String tokenHints, String tableName1, String tableName2)
        throws SQLException {
        Assert.assertEquals(sql, getDdlRecordSql(tokenHints));
        Assert.assertEquals(2, getDdlRecordSqlCount(tokenHints));
        Assert.assertEquals(getDdlRecordTopology(tokenHints, tableName1), queryTopology(tableName1));
        Assert.assertEquals(getDdlRecordTopology(tokenHints, tableName2), queryTopology(tableName2));
    }

    private String getAddPartitionSql(String tableGroup, PartitionType partitionType) {
        if (partitionType == PartitionType.Range) {
            return String
                .format("ALTER TABLEGROUP %s ADD PARTITION (PARTITION p4 VALUES LESS THAN (2030))", tableGroup);
        } else if (partitionType == PartitionType.RangeColumn) {
            return String
                .format("ALTER TABLEGROUP %s ADD PARTITION (PARTITION p5 VALUES LESS THAN ('2041-01-01'))", tableGroup);
        } else if (partitionType == PartitionType.List) {
            return String
                .format("ALTER TABLEGROUP %s ADD PARTITION (PARTITION p5 VALUES IN (2060,2062))",
                    tableGroup);
        } else if (partitionType == PartitionType.ListColumn) {
            return String
                .format("ALTER TABLEGROUP %s ADD PARTITION (PARTITION p5 VALUES IN ('2060-01-01','2062-01-01'))",
                    tableGroup);
        }

        throw new RuntimeException("not supported partition type " + partitionType);
    }

    private String getDropPartitionSql(String tableGroup, PartitionType partitionType) {
        return String.format("ALTER TABLEGROUP %s DROP PARTITION p4", tableGroup);
    }

    private List<String> getSplitPartitionSql(String tableGroup, String tableName, PartitionType partitionType)
        throws SQLException {
        if (partitionType == PartitionType.Range) {
            return Lists.newArrayList(String
                .format("ALTER TABLEGROUP %s split PARTITION p2 INTO "
                    + "(PARTITION p21 VALUES LESS THAN (2005),PARTITION p22 VALUES LESS THAN (2010))", tableGroup));
        } else if (partitionType == PartitionType.RangeColumn) {
            return Lists.newArrayList(String.
                format("ALTER TABLEGROUP %s split PARTITION p2 into "
                        + "(PARTITION p21 VALUES LESS THAN ('2005-01-01'), PARTITION p22 VALUES LESS THAN ('2011-01-01'))",
                    tableGroup));
        } else if (partitionType == PartitionType.Hash) {
            List<String> list = getPartitionList(tableName);
            return Lists.newArrayList(
                String.format("ALTER TABLEGROUP %s SPLIT PARTITION %s", tableGroup, list.get(0))
            );
        } else if (partitionType == PartitionType.HashKey) {
            List<String> list = getPartitionList(tableName);
            return Lists.newArrayList(
                String.format("ALTER TABLEGROUP %s SPLIT PARTITION %s", tableGroup, list.get(0))
            );
        } else if (partitionType == PartitionType.List) {
            return Lists.newArrayList(
                String.format("ALTER TABLEGROUP %s SPLIT PARTITION p2 INTO "
                        + "(PARTITION p21 VALUES in (2010,2012), PARTITION p22 VALUES in (2013))",
                    tableGroup)
            );
        } else if (partitionType == PartitionType.ListColumn) {
            return Lists.newArrayList(
                String.format("ALTER TABLEGROUP %s SPLIT PARTITION p2 INTO "
                        + "(PARTITION p21 VALUES in ('2010-01-01','2012-01-01'), PARTITION p22 VALUES in ('2013-01-01'))",
                    tableGroup)
            );
        }
        throw new RuntimeException("not supported partition type " + partitionType);
    }

    private String getMergePartitionSql(String tableGroup, PartitionType partitionType) {
        return String.format("ALTER TABLEGROUP %s merge PARTITIONS p3,p4 to p4", tableGroup);
    }

    private String getMovePartitionSql(String tableGroup, String tableName, PartitionType partitionType)
        throws SQLException {
        Map<String, String> map = getMasterGroupStorageMap();
        String fromGroup = null;
        String fromStorage = null;
        String toStorage = null;
        String partition = null;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String temp = getOnePartitionByGroupName(entry.getKey(), tableName);
            if (StringUtils.isNotBlank(temp)) {
                fromStorage = entry.getValue();
                partition = temp;
                break;
            }
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!StringUtils.equals(fromStorage, entry.getValue())) {
                toStorage = entry.getValue();
            }
        }

        return String
            .format("ALTER TABLEGROUP %s move PARTITIONS %s to '%s'", tableGroup, partition, toStorage);
    }

    private String getTruncatePartitionSql(String tableName, PartitionType partitionType) throws SQLException {
        List<String> list = getPartitionList(tableName);
        return String.format("ALTER TABLE %s TRUNCATE PARTITION %s", tableName, list.get(0));
    }

    enum PartitionType {
        Hash, HashKey, List, ListColumn, Range, RangeColumn, Single, Broadcast;

        boolean isSupportAddPartition() {
            return this == Range || this == RangeColumn || this == List || this == ListColumn;
        }

        boolean isSupportDropPartition() {
            return this == Range || this == RangeColumn || this == List || this == ListColumn;
        }

        boolean isSupportMovePartition() {
            return this == Range || this == RangeColumn || this == List || this == ListColumn
                || this == Hash || this == HashKey;
        }

        boolean isPartitionTable() {
            return this != Single && this != Broadcast;
        }
    }
}