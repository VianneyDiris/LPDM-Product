package com.lpdm.msproduct.controller;


import com.lpdm.msproduct.dao.ProductDao;
import com.lpdm.msproduct.entity.Category;
import com.lpdm.msproduct.entity.Product;
import com.lpdm.msproduct.entity.Stock;
import com.lpdm.msproduct.exception.ProducerNotFound;
import com.lpdm.msproduct.exception.ProductNotFound;
import com.lpdm.msproduct.proxy.ProducerProxy;
import com.lpdm.msproduct.proxy.StockProxy;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Vianney
 * @version 1.0
 * @since 07/12/2018
 */

@Api(description="Controller pour les opérations CRUD sur les products.")
@RestController
public class ProductController {
    private Logger log = LogManager.getLogger(this.getClass());

    @Autowired
    private ProductDao productDao;

    @Autowired
    private StockProxy stockProxy;

    @Autowired
    private ProducerProxy producerProxy;


    /**
     * Call this method to get an {@link List<Product>}
     * @return An {@link List<Product>} json object
     */
    @ApiOperation(value = "Récupère tous les produits de la bdd")
    @GetMapping(value = "/products", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Product> listProduct(){
        log.info("ProductController -> méthode listProduct : entrée ");
        List<Product> list = productDao.findAll();

        if (list == null){
            throw new ProductNotFound("Aucun produit trouvé dans la base de données");
        }

        for(Product product : list){
            log.info("ProductController -> méthode listProduct : boucle ");
            product.setListStock(stockProxy.listStockByProductId(product.getId()));
            product.setProducer(producerProxy.findById(product.getProducerID()));
        }
        log.debug("ProductController -> méthode listProduct : test list vide = "+list.size());
        log.info("ProductController -> méthode listProduct : sortie ");
        return list;
    }

    /**
     * Find {@link Product} by the product {@link Integer} id
     * @param id The {@link Product} {@link Integer} id
     * @return an {@link Product} json object
     */
    @ApiOperation(value = "Récupère un produit grâce à son ID si celui-ci existe dans la bdd")
    @GetMapping(value="/products/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Product findProduct(@PathVariable int id){
        log.info("ProductController -> méthode findProduct : entrée ");
        log.info("ProductController -> méthode findProduct : id envoyé = "+id);
        Product product = productDao.findById(id);

        if (product == null){
            throw new ProductNotFound("Aucun produit trouvé pour l'id = "+id);
        }
        product.setListStock(stockProxy.listStockByProductId(product.getId()));
        product.setProducer(producerProxy.findById(product.getProducerID()));
        log.debug("ProductController -> méthode findProduct : test Producer = "+product.getProducer().getName());

        log.info("ProductController -> méthode findProduct : product envoyé = "+product.toString());
        log.info("ProductController -> méthode findProduct : sortie ");
        return product;
    }

    /**
     * Add {@link Product} in database
     * @param product {@link Product}
     * @return productAdded {@link Product}
     */
    @ApiOperation(value = "Enregistre un produit si celui-ci est conforme")
    @PostMapping(value = "/products", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Product addProduct(@Valid @RequestBody Product product){
        log.info("ProductController -> méthode addProduct : entrée ");
        log.info("ProductController -> méthode addProduct : product reçu = "+product.toString());
        if(product.getProducer() == null || product.getProducer().getId() == 0){
            throw new ProducerNotFound("L'objet Producer ne peut être null");
        }
        product.setProducerID(product.getProducer().getId());
        log.debug("ProductController -> méthode findProduct : test ProducerId = "+product.getProducer().getId());
        Product productAdded = productDao.save(product);

        if (productAdded.equals(null)){
            log.debug("ProductController -> méthode findProduct : erreur lors de l'ajout");
        }
        log.info("ProductController -> méthode addProduct : sortie ");

        return productAdded;
    }

    /**
     * Delete {@link Product} by the product {@link Integer} id
     * @param id The {@link Product} {@link Integer} id
     */
    @ApiOperation(value = "Supprime un produit grâce à son ID si celui-ci existe dans la bdd")
    @DeleteMapping(value="/products/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void deleteProduct(@PathVariable int id){
        log.info("ProductController -> méthode deleteProduct : entrée ");
        log.info("ProductController -> méthode deleteProduct : id envoyé = "+id);
        Product productDelete = productDao.findById(id);
        productDao.deleteById(id);

        if(!productDelete.getListStock().equals(null)){
            for(Stock stockDelete : productDelete.getListStock()){
                stockProxy.deleteStock(stockDelete.getId());
            }
        }
        log.info("ProductController -> méthode deleteProduct : sortie ");
    }

    /**
     * Update {@link Product} in database
     * @param {@link Product} product
     */
    @ApiOperation(value = "Met à jour un produit si celui-ci est conforme")
    @PutMapping(value="/products", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updateProduct(@Valid @RequestBody Product product){
        log.info("ProductController -> méthode updateProduct : entrée ");
        log.info("ProductController -> méthode updateProduct : product reçu = "+product.toString());

        if(product.getProducer() == null || product.getProducer().getId() == 0){
            throw new ProducerNotFound("L'objet Producer ne peut être null");
        }

        product.setProducerID(product.getProducer().getId());
        log.debug("ProductController -> méthode updateProduct : test ProducerID = "+product.getProducer().getId());
        productDao.save(product);
/*
        if(!product.getListStock().equals(null)){
            for(Stock stockUpdate : product.getListStock()){
                stockProxy.updateStock(stockUpdate);
            }
        }
        */
        log.info("ProductController -> méthode updateProduct : sortie ");
    }

    /**
     * Find {@link List<Product>} by the Category {@link Integer} id
     * @param id The {@link Category} {@link Integer} id
     * @return an {@link List<Product>} json object
     */
    @ApiOperation(value = "Récupère tous les produits en fonction de l'id de la categorie")
    @GetMapping(value = "/products/category/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Product> listProductByCategoryById(@PathVariable int id){
        log.info("ProductController -> méthode listProductByCategoryById : entrée ");
        log.info("ProductController -> méthode listProductByCategoryById : id envoyé = "+id);
        List<Product> listProducts = productDao.findByCategoryId(id);

        if (listProducts == null){
            throw new ProductNotFound("Aucun produit trouvé pour la catégory ayant pour id = "+id);
        }

        log.debug("ProductController -> méthode listProductByCategoryById : test listProducts = "+listProducts.size());

        for(Product product : listProducts){
            log.info("ProductController -> méthode listProductByCategoryById : boucle ");
            product.setListStock(stockProxy.listStockByProductId(product.getId()));
            product.setProducer(producerProxy.findById(product.getProducerID()));
        }
        log.info("ProductController -> méthode listProductByCategoryById : sortie ");
        return listProducts;
    }

    /**
     * Find {@link List<Product>} by the Category {@link Category}
     * @param category The {@link Category}
     * @return an {@link List<Product>} json object
     */
    @ApiOperation(value = "Récupère tous les produits en fonction de la categorie")
    @PostMapping(value = "/products/category", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Product> listProductByCategory(@RequestBody Category category){
        log.info("ProductController -> méthode listProductByCategory : entrée ");
        log.info("ProductController -> méthode listProductByCategory : category reçu = "+category.toString());
        List<Product> listProducts = productDao.findByCategoryId(category.getId());

        if (listProducts == null){
            throw new ProductNotFound("Aucun produit trouvé pour la catégory = "+category.getName());
        }

        for(Product product : listProducts){
            log.info("ProductController -> méthode listProductByCategory : boucle ");
            product.setListStock(stockProxy.listStockByProductId(product.getId()));
            product.setProducer(producerProxy.findById(product.getProducerID()));
        }
        log.info("ProductController -> méthode listProductByCategory : sortie ");
        return listProducts;
    }


    /**
     * Find {@link List<Product>} by the producer {@link Integer} id
     * @param id The {@link com.lpdm.msproduct.entity.Producer} {@link Integer} id
     * @return an {@link List<Product>} json object
     */
    @ApiOperation(value = "Récupère tous les produits en fonction de l'id du producteur")
    @GetMapping(value = "/products/producer/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Product> listProductByProducerId(@PathVariable int id){
        log.info("ProductController -> méthode listProductByProducerId : entrée ");
        log.info("ProductController -> méthode listProductByProducerId : id envoyé = "+id);
        List<Product> listProducts = productDao.findByProducerID(id);

        if (listProducts == null){
            throw new ProductNotFound("Aucun produit trouvé pour le producer ayant l'id = "+id);
        }

        for(Product product : listProducts){
            log.info("ProductController -> méthode listProductByProducerId : boucle ");
            product.setListStock(stockProxy.listStockByProductId(product.getId()));
            product.setProducer(producerProxy.findById(product.getProducerID()));
        }
        log.info("ProductController -> méthode listProductByProducerId : sortie ");
        return listProducts;
    }

    /**
     * Find {@link List<Product>} by the product {@link String} name
     * @param name The {@link Product} {@link String} name
     * @return an {@link List<Product>} json object
     */
    @ApiOperation(value = "Récupère tous les produits en fonction du nom de celui-ci")
    @GetMapping(value = "/products/name/{name}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Product> listProductByName(@PathVariable String name){
        log.info("ProductController -> méthode listProductByName : entrée ");
        log.info("ProductController -> méthode listProductByName : name envoyé = "+name);
        List<Product> listProducts = productDao.findAllByNameContainingIgnoreCase(name);

        if (listProducts == null){
            throw new ProductNotFound("Aucun produit trouvé ayant pour name = "+name);
        }

        for(Product product : listProducts){
            log.info("ProductController -> méthode listProductByName : entrée ");
            product.setListStock(stockProxy.listStockByProductId(product.getId()));
            product.setProducer(producerProxy.findById(product.getProducerID()));
        }

        log.info("ProductController -> méthode listProductByName : sortie ");
        return listProducts;
    }


    /**
     * Find {@link Page<Product>} by the product
     * @param pageable The {@link Product}
     * @return an {@link Page<Product>} json object
     */
    @ApiOperation(value = "Récupère tous les produits en fonction du pageable")
    @GetMapping(value = "/listPageable",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    Page<Product> productsPageable(Pageable pageable) {
        return productDao.findAll(pageable);

    }
}
