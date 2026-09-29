package com.vengalarao.enterprise_order_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vengalarao.enterprise_order_platform.entity.Product;

//What does the two generic types mean?
//Product: is the entity that we are working on and Long is the primary key of that entity.

//Why an interface (i.e why not provide the implementation details as ProductRepositoryImpl?)
//Spring Data JPA generates the implementation at runtime. Spring creates something like
/*
class GeneratedProductRepository {
    //save (Product product)
    //findById (Long id)
    //findAll ()
    //deleteById (Long id)
    //existsById (Long id)
    //count()
}
    Spring then registers that generated object as a bean so it can be injected in service layer.
    All the methods are inherited from JpaRepository.
*/


public interface ProductRepository extends JpaRepository<Product, Long> {

    //DB contraint we specified protects the table, Detect duplicates in the service layer and return a meaningful API error before MySQL throws a constraint violation.
    //How does this method work without implementation?
    /*
    Spring Data reads method name and interprets pieces as exists Name IgnoreCase and understands that return whether a matching row exists -> use the Product.name field -> compare without considering letter case.
    */
    boolean existsByNameIgnoreCase(String name);

    /* for PUT i.e updating a product
        This repository method is used. This method ensures that there is no other product with same name.
        Checks if there is a product with same name ignoring the case AND id is different.
        Because we might have cases where we update other product attributes apart from name so we should also check that id's of both the products (with matching name) are different.
    */
   boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

}
