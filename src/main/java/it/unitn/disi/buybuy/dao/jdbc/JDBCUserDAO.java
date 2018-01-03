/*
 * JDBCUserDAO.java
 */
package it.unitn.disi.buybuy.dao.jdbc;

import it.unitn.aa1617.webprogramming.persistence.utils.dao.exceptions.DAOException;
import it.unitn.aa1617.webprogramming.persistence.utils.dao.jdbc.JDBCDAO;
import it.unitn.disi.buybuy.dao.UserDAO;
import it.unitn.disi.buybuy.dao.entities.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The JDBC implementation of the {@link UserDAO} interface.
 *
 * @author apello96
 */
public class JDBCUserDAO extends JDBCDAO<User, Integer> implements UserDAO {

    public JDBCUserDAO(Connection con) {
        super(con);
    }

    /**
     * Returns the number of {@link User users} stored on the persistence system
     * of the application.
     *
     * @return the number of records present into the storage system.
     * @throws DAOException if an error occurred during the information
     * retrieving.
     *
     * @author apello96
     */
    @Override
    public Long getCount() throws DAOException {
        try (PreparedStatement stmt = CON.prepareStatement("SELECT COUNT(*) FROM app.USER_DETAIL");) {
            ResultSet counter = stmt.executeQuery();
            if (counter.next()) {
                return counter.getLong(1);
            }
        } catch (SQLException ex) {
            throw new DAOException("Impossible to count users", ex);
        }

        return 0L;
    }

    /**
     * Returns the {@link User user} with the primary key equals to the one
     * passed as parameter.
     *
     * @param primaryKey the {@code id} of the {@code user} to get.
     * @return the {@code user} with the id equals to the one passed as
     * parameter or {@code null} if no entities with that id is not present into
     * the storage system.
     * @throws DAOException if an error occurred during the information
     * retrieving.
     *
     * @author apello96
     */
    @Override
    public User getByPrimaryKey(Integer primaryKey) throws DAOException {
        if (primaryKey == null) {
            throw new DAOException("primaryKey is null");
        }
        try (PreparedStatement stm = CON.prepareStatement("SELECT * FROM app.USER_DETAIL WHERE id = ?")) {
            stm.setInt(1, primaryKey);
            try (ResultSet rs = stm.executeQuery()) {

                rs.next();
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setHashPassword(rs.getString("hash_password"));
                user.setName(rs.getString("name"));
                user.setLastname(rs.getString("lastname"));
                user.setEmail(rs.getString("email"));
                user.setType(User.Type.valueOf(rs.getString("type")));

                return user;
            }
        } catch (SQLException ex) {
            throw new DAOException("Impossible to get the user for the passed primary key", ex);
        }
    }

    /**
     * Returns the {@link User user} with the given {@code username} and
     * {@code hashPassword}.
     *
     * @param username the username of the user to get.
     * @param hashPassword the hashPassword of the user to get.
     * @return the {@link User user} with the given {@code username} and
     * {@code hashPassword}..
     * @throws DAOException if an error occurred during the information
     * retrieving.
     *
     * @author apello96
     */
    public User getByUsernameAndPassword(String username, String hashPassword) throws DAOException {
        if ((username == null) || (hashPassword == null)) {
            throw new DAOException("Username and password are mandatory fields", new NullPointerException("username or password are null"));
        }

        try (PreparedStatement stm = CON.prepareStatement("SELECT * FROM app.USER_DETAIL WHERE username = ? AND hash_password = ?")) {
            stm.setString(1, username);
            stm.setString(2, hashPassword);
            try (ResultSet rs = stm.executeQuery()) {

                int count = 0;
                while (rs.next()) {
                    count++;
                    if (count > 1) {
                        throw new DAOException("Unique constraint violated! There are more than one user with the same username! WHY???");
                    }
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setHashPassword(rs.getString("hash_password"));
                    user.setHashSalt("hash_salt");
                    user.setName(rs.getString("name"));
                    user.setLastname(rs.getString("lastname"));
                    user.setEmail(rs.getString("email"));
                    user.setType(User.Type.valueOf(rs.getString("type")));

                    return user;
                }

                return null;
            }
        } catch (SQLException ex) {
            throw new DAOException("Impossible to get the user", ex);
        }
    }

    /**
     * Returns the list of all the valid {@link User users} stored by the
     * storage system.
     *
     * @return the list of all the valid {@code users}.
     * @throws DAOException if an error occurred during the information
     * retrieving.
     *
     * @author apello96
     */
    @Override
    public List<User> getAll() throws DAOException {
        List<User> users = new ArrayList<>();

        try (PreparedStatement stm = CON.prepareStatement("SELECT * FROM app.USER_DETAIL ORDER BY lastname")) {
            try (ResultSet rs = stm.executeQuery()) {

                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setHashPassword(rs.getString("hash_password"));
                    user.setHashSalt("hash_salt");
                    user.setName(rs.getString("name"));
                    user.setLastname(rs.getString("lastname"));
                    user.setEmail(rs.getString("email"));
                    user.setType(User.Type.valueOf(rs.getString("type")));
                    
                    users.add(user);
                }
            }
        } catch (SQLException ex) {
            throw new DAOException("Impossible to get the list of users", ex);
        }

        return users;
    }

    /**
     * Update the user passed as parameter and returns it.
     *
     * @param user the user used to update the persistence system.
     * @return the updated user.
     * @throws DAOException if an error occurred during the action.
     *
     * @author apello96
     */
    @Override
    public User update(User user) throws DAOException {
        if (user == null) {
            throw new DAOException("parameter not valid", new IllegalArgumentException("The passed user is null"));
        }

        try (PreparedStatement std = CON.prepareStatement("UPDATE app.USER_DETAIL SET username = ?, hash_password = ?, hash_salt = ?, name = ?, lastname = ?, email = ?, user_type = ? WHERE id = ?")) {
            std.setString(1, user.getUsername());
            std.setString(2, user.getHashPassword());
            std.setString(3, user.getHashSalt());
            std.setString(4, user.getName());
            std.setString(5, user.getLastname());
            std.setString(6, user.getEmail());
            std.setString(7, user.getType().name());
            std.setInt(8, user.getId());
            if (std.executeUpdate() == 1) {
                return user;
            } else {
                throw new DAOException("Impossible to update the user");
            }
        } catch (SQLException ex) {
            throw new DAOException("Impossible to update the user", ex);
        }
    }

    /**
     * Persists the new {@link User user} passed as parameter to the storage
     * system.
     * @param user the new {@code user} to persist.
     * @return the id of the new persisted record. 
     * @throws DAOException if an error occurred during the persist action.
     * 
     * @author apello96
     */
    @Override
    public Long insert(User user) throws DAOException {
        try (PreparedStatement ps = CON.prepareStatement("INSERT INTO app.USER_DETAIL(name,lastname,username,email,hash_password,hash_salt,user_type) VALUES(?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, user.getName());
            ps.setString(2, user.getLastname());
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getHashPassword());
            ps.setString(6, user.getHashSalt());
            ps.setInt(7, user.getType().ordinal());

            if (ps.executeUpdate() == 1) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    try {
                        CON.rollback();
                    } catch (SQLException ex) {
                        //User: log the exception
                        Logger.getLogger(JDBCUserDAO.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    throw new DAOException("Impossible to persist the new user");
                }
            } else {
                try {
                    CON.rollback();
                } catch (SQLException ex) {
                    //User: log the exception
                    Logger.getLogger(JDBCUserDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
                throw new DAOException("Impossible to persist the new user");
            }
        } catch (SQLException ex) {
            try {
                CON.rollback();
            } catch (SQLException ex1) {
                //User: log the exception
                Logger.getLogger(JDBCUserDAO.class.getName()).log(Level.SEVERE, null, ex1);
            }
            throw new DAOException("Impossible to persist the new user", ex);
        }
    }
}
