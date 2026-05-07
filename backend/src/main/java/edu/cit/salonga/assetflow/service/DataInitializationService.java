package edu.cit.salonga.assetflow.service;

import edu.cit.salonga.assetflow.features.assets.entity.Asset;
import edu.cit.salonga.assetflow.features.assets.entity.Category;
import edu.cit.salonga.assetflow.features.assets.repository.AssetRepository;
import edu.cit.salonga.assetflow.features.assets.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("never") // Disabled - remove "never" to enable
public class DataInitializationService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Bean
    public ApplicationRunner initializeData() {
        return args -> {
            System.out.println("⚠️  Data initialization is currently disabled");
        };
    }
}
