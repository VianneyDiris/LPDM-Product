package com.lpdm.msproduct.controller;

import com.lpdm.msproduct.dao.CategoryDao;
import com.lpdm.msproduct.entity.Category;
import com.lpdm.msproduct.exception.CategoryNotFound;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Vianney
 * @version 1.0
 * @since 07/12/2018
 */

@Api(description="Controller pour les opérations CRUD sur les catégories.")
@RestController
public class CategoryController {
    private Logger log = LogManager.getLogger(this.getClass());

    @Autowired
    private CategoryDao categoryDao;

    /**
     * Call this method to get an {@link List<Category>}
     * @return An {@link List<Category>} json object
     */
    @ApiOperation(value = "Récupère tous les catégories de la bdd")
    @GetMapping(value = "/categories", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Category> listCategories(){
        
        List<Category> listCategory = categoryDao.findAll();

        if (listCategory == null){
            throw new CategoryNotFound("Aucune catégory trouvé dans la base de données");
        }
        log.debug("CategoryController -> méthode listCategories : test listCategory = "+listCategory.size());

        return listCategory;
    }


    /**
     * Find {@link Category} by the category {@link Integer} id
     * @param id The {@link Category} {@link Integer} id
     * @return an {@link Category} json object
     */
    @ApiOperation(value = "Récupère une catégorie grâce à son ID si celui-ci existe dans la bdd")
    @GetMapping(value = "/categories/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Category category(@PathVariable int id){
        
        Category category = categoryDao.findById(id);

        if (category == null){
            throw new CategoryNotFound("Aucune catégory trouvé ayant l'id = "+id);
        }
        log.debug("CategoryController -> méthode category : test category = "+category.getId());

        return category;
    }


    /**
     * Add {@link Category} in database
     * @param category {@link Category}
     */
    @ApiOperation(value = "Enregistre une catégorie si celle-ci est conforme")
    @PostMapping(value = "/categories", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void addCategory(@Valid @RequestBody Category category){
        
        Category categoryAdded = categoryDao.save(category);

        if(categoryAdded.equals(null)){
            log.debug("CategoryController -> méthode addCategory : Problème lors de l'ajout de la catégorie ");
        }
    }

    /**
     * Delete {@link Category} by the category {@link Integer} id
     * @param id The {@link Category} {@link Integer} id
     */
    @ApiOperation(value = "Supprime une catégorie grâce à son ID si celui-ci existe dans la bdd")
    @DeleteMapping(value="/categories/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void deleteCategory(@PathVariable int id){
        
        categoryDao.deleteById(id);
        
    }

    /**
     * Update {@link Category} in database
     * @param category {@link Category}
     */
    @ApiOperation(value = "Met à jour une catégorie si celle-ci est conforme")
    @PutMapping(value = "/categories", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updateCategory(@Valid @RequestBody Category category){
        
        categoryDao.save(category);
        
    }

}
