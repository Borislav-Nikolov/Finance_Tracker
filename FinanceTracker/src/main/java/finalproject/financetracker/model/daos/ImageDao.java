package finalproject.financetracker.model.daos;

import finalproject.financetracker.controller.SpringJdbcConfig;
import finalproject.financetracker.model.Image;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;

public final class ImageDao {
    private static JdbcTemplate jdbcTemplate = new JdbcTemplate(SpringJdbcConfig.mysqlDataSource());
    private static String ICONS_RELATIVE_PATH = "src/main/resources/static/img/category_icons";
    private ImageDao() {}
    public static Image getImageById(long id) {
        String sql = "SELECT uri FROM final_project.images WHERE image_id = ?;";
        return jdbcTemplate.queryForObject(sql, new Object[] {id}, new BeanPropertyRowMapper<>(Image.class));
    }
    public static Image getImageByFileName(String fileName) {
        String sql = "SELECT uri FROM final_project.images WHERE uri = ?;";
        String uri = ICONS_RELATIVE_PATH + "/" + fileName;
        return jdbcTemplate.queryForObject(sql, new Object[] {uri}, new BeanPropertyRowMapper<>(Image.class));
    }
    public static void addImage(String fileName) {
        String uri = ICONS_RELATIVE_PATH + "/" + fileName;
        String sql = "INSERT INTO final_project.images(uri) " +
                "VALUES (?);";
        jdbcTemplate.update(sql, uri);
    }

    public static void addAllIcons() {
        File dir = new File(ICONS_RELATIVE_PATH);
        for (File file : dir.listFiles()) {
            addImage(file.getName());
        }
    }
}
