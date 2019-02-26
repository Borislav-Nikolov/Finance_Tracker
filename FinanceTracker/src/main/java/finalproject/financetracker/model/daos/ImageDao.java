package finalproject.financetracker.model.daos;

import finalproject.financetracker.model.pojos.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ImageDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static String ICONS_RELATIVE_PATH = "src/main/resources/static/img/category_icons";
    public  Image getImageById(long id) {
        String sql = "SELECT * FROM final_project.images WHERE image_id = ?;";
        return jdbcTemplate.queryForObject(sql, new Object[] {id}, new BeanPropertyRowMapper<>(Image.class));
    }
    public Image getImageByFileName(String fileName) {
        String sql = "SELECT * FROM final_project.images WHERE uri LIKE ?;";
        String uri = ICONS_RELATIVE_PATH + "/" + fileName;
        Image image = null;
        try {
            image = jdbcTemplate.queryForObject(sql, new Object[] {uri}, new BeanPropertyRowMapper<>(Image.class));
        } catch (DataAccessException ex) {
            System.out.println("Image not found: " + ex.getMessage());
        }
        return image;
    }
    public void addImage(String fileName) {
        String uri = ICONS_RELATIVE_PATH + "/" + fileName;
        String sql = "INSERT INTO final_project.images(uri) " +
                "VALUES (?);";
        jdbcTemplate.update(sql, uri);
    }

    public void addAllIcons() {
        File dir = new File(ICONS_RELATIVE_PATH);
        for (File file : dir.listFiles()) {
            addImage(file.getName());
        }
    }
}
