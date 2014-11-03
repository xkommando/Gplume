package com.caibowen.gplume.common.test;

import com.caibowen.gplume.jdbc.JdbcSupport;
import com.caibowen.gplume.jdbc.StatementCreator;
import com.caibowen.gplume.jdbc.mapper.RowMapping;
import com.caibowen.gplume.jdbc.transaction.Transaction;
import com.caibowen.gplume.jdbc.transaction.TransactionCallback;

import javax.activation.DataSource;
import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author BowenCai
 * @since 3-11-2014.
 */
public class JdbcExample {

    public void s() {
        final Model model2;
        final List<Model> models;
        DataSource dataSource = null;


        final JdbcSupport jdbcSupport = new JdbcSupport();

        jdbcSupport.setDataSource(dataSource);
        List<String> ids = jdbcSupport.batchInsert(new StatementCreator() {
            @Nonnull
            @Override
            public PreparedStatement createStatement(@Nonnull Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO `model_talbe`(`id, `name`)VALUES (?,?)");
                for (Model model : models) {
                    ps.setString(1, model.getId());
                    ps.setString(2, model.getName());
                    ps.addBatch();
                }
                return ps;
            }
        }, new String[]{"id"}, RowMapping.STR_ROW_MAPPING) ;

        List<Model> ls = jdbcSupport.queryForList(new StatementCreator() {
            @Nonnull
            @Override
            public PreparedStatement createStatement(@Nonnull Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM `table_model`");
                return ps;
            }
        }, new RowMapping<Model>() {
            @Override
            public Model extract(@Nonnull ResultSet rs) throws SQLException {
                Model model = new Model();
                model.setId(rs.getString(1))
                model.setName(rs.getString(2));
                return model;
            }
        });

        jdbcSupport.execute(new TransactionCallback<Object>() {
            @Override
            public Object withTransaction(@Nonnull Transaction tnx) throws Exception {
                tnx.setRollbackOnly(true);
                // operation 1
                // operation 2
                jdbcSupport.execute(new TransactionCallback<Object>() {
                    @Override // nested transaction is isolated from the outer one
                    public Object withTransaction(@Nonnull Transaction tnx) throws Exception {
                        tnx.setRollbackOnly(true);
                        throw new Exception();
                    }
                });
                // fail on any of the operations will trigger rollback automatically
                return null;
            }
        });
    }

    class Model {
        String id;
        String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
