package edu.cit.salonga.assetflow.features.assets.config;

import edu.cit.salonga.assetflow.features.assets.entity.Category;
import edu.cit.salonga.assetflow.features.assets.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategorySeeder implements CommandLineRunner {

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Electronics",
            "Laboratory Equipment",
            "Furniture",
            "Tools",
            "Audio/Visual",
            "Computing",
            "Sports Equipment",
            "Other"
    );

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        for (String name : DEFAULT_CATEGORIES) {
            if (categoryRepository.findByName(name) == null) {
                Category category = new Category();
                category.setName(name);
                categoryRepository.save(category);
            }
        }
    }
}
