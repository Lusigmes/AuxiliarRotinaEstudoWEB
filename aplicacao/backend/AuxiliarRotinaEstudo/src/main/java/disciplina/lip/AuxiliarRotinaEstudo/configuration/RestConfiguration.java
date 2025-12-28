package disciplina.lip.AuxiliarRotinaEstudo.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import disciplina.lip.AuxiliarRotinaEstudo.repository.RelatorioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Type;

@Configuration
@EnableWebMvc 
public class RestConfiguration implements RepositoryRestConfigurer, WebMvcConfigurer   {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        config.disableDefaultExposure();
        cors.addMapping("/**")
            .allowedOrigins("*") 
            .allowedMethods("*") 
            .allowedHeaders("*");
        config.exposeIdsFor(RelatorioRepository.class);
        EntityManager em = entityManagerFactory.createEntityManager();
        config.exposeIdsFor(em.getMetamodel().getEntities().stream().map(Type::getJavaType).toArray(Class[]::new));

    }

}