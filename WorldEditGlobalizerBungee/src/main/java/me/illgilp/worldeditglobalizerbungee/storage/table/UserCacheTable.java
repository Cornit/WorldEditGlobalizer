package me.illgilp.worldeditglobalizerbungee.storage.table;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import me.illgilp.worldeditglobalizerbungee.storage.WhereBuilder;
import me.illgilp.worldeditglobalizerbungee.storage.model.UserCacheModel;

public class UserCacheTable implements Table<UserCacheModel, UUID> {

    private Dao<UserCacheModel, UUID> dao;

    @Override
    public boolean init(ConnectionSource source) {
        try {
            dao = DaoManager.createDao(source, UserCacheModel.class);
            TableUtils.createTableIfNotExists(source, UserCacheModel.class);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }

    @Override
    public boolean isInitialized() {
        return dao != null;
    }

    @Override
    public boolean add(UserCacheModel instance) {
        if(isInitialized()) {
            try {
                dao.create(instance);
            } catch (SQLException e) {
                return false;
            }
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean exists(UserCacheModel instance) {
        if(isInitialized()) {
            List<UserCacheModel> payments = new ArrayList<>();

            try {
                payments= dao.queryForMatching(instance);
            } catch (SQLException e) {
                return false;
            }

            return !payments.isEmpty();
        }
        return false;
    }

    @Override
    public boolean update(UserCacheModel instance) {
        if(isInitialized()) {
            try {
                dao.update(instance);
            } catch (SQLException e) {
                return false;
            }
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean createOrUpdate(UserCacheModel instance) {
        if(isInitialized()) {
            try {
                dao.createOrUpdate(instance);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean remove(UserCacheModel instance) {
        if(isInitialized()){
            try {
                List<UserCacheModel> list = get(instance);
                if(list.size() == 0)return false;
                for(UserCacheModel model : list) {
                    int resp = dao.delete(model);
                    if(resp == 0)return false;
                }
                return true;
            } catch (SQLException e) {
                return false;
            }
        }else{
            return false;
        }
    }

    @Override
    public UserCacheModel getExact(UserCacheModel instance) {

        List<UserCacheModel> models = this.get(instance);

        if (models.size() == 0) {
            return null;
        } else {
            return models.get(0);
        }
    }

    @Override
    public List<UserCacheModel> get(UserCacheModel instance) {
        if (isInitialized()) {
            try {
                return dao.queryForMatching(instance).stream().sorted((m1,m2) -> m2.getLastUpdate().compareTo(m1.getLastUpdate())).collect(Collectors.toList());
            } catch (SQLException e) {
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<UserCacheModel> getAll() {
        if (isInitialized()) {
            try {
                return dao.queryForAll();
            } catch (SQLException e) {
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public long sizeOf(Map<String, Object> map) {
        if (isInitialized()) {
            try {
                QueryBuilder<UserCacheModel, UUID> queryBuilder = dao.queryBuilder();
                Where<UserCacheModel, UUID> where = queryBuilder.where();
                for (Map.Entry<String, Object> stringObjectEntry : map.entrySet()) {
                    where.eq(stringObjectEntry.getKey(), stringObjectEntry.getValue());
                }
                return queryBuilder.countOf();
            } catch (SQLException e) {
                return 0;
            }
        }
        return 0;
    }

    @Override
    public long sizeOf(WhereBuilder<UserCacheModel, UUID> builder) {
        if (isInitialized()) {
            try {
                QueryBuilder<UserCacheModel, UUID> queryBuilder = dao.queryBuilder();
                Where<UserCacheModel, UUID> where = queryBuilder.where();
                builder.build(where);
                return where.countOf();
            } catch (Exception e) {
                if (e instanceof SQLException) {
                    return 0;
                } else {
                    e.printStackTrace();
                    return 0;
                }
            }
        }
        return 0;
    }

    @Override
    public long size() {
        if (isInitialized()) {
            try {
                return dao.countOf();
            } catch (SQLException e) {
                return 0;
            }
        }
        return 0;
    }

    public Dao<UserCacheModel, UUID> getDao() {
        return dao;
    }
}
